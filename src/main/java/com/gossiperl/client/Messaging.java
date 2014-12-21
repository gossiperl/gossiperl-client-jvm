package com.gossiperl.client;

import com.gossiperl.client.serialization.*;
import com.gossiperl.client.thrift.Digest;
import com.gossiperl.client.thrift.DigestAck;
import com.gossiperl.client.thrift.DigestMember;
import com.gossiperl.client.transport.Udp;
import org.apache.log4j.Logger;
import org.apache.thrift.TBase;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class Messaging {

    public enum OutgoingDataType {
        DIGEST,
        KILL_PILL
    }

    private OverlayWorker worker;
    private Udp transport;

    private LinkedBlockingQueue<DeserializeResult> incomingQueue;
    private LinkedBlockingQueue<OutgoingData> outgoingQueue;

    private static Logger LOG = Logger.getLogger(Messaging.class);

    public Messaging(OverlayWorker worker) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        this.worker = worker;
        this.incomingQueue = new LinkedBlockingQueue<DeserializeResult>();
        this.outgoingQueue = new LinkedBlockingQueue<OutgoingData>();
        this.transport = new Udp( this.worker );
        LOG.info("[" + this.worker.getConfiguration().getClientName() + "] Messaging initialized.");
    }

    public void start() {
        (new Thread(this.transport)).start();
        (new Thread(new IncomingQueueWorker())).start();
        (new Thread(new OutgoingQueueWorker())).start();
    }

    public void stop() {
        try {
            this.outgoingQueue.offer(new OutgoingData(OutgoingDataType.KILL_PILL));
        } catch (GossiperlClientException ex) {
            LOG.warn("[" + this.worker.getConfiguration().getClientName() + "] Received an unexpected exception while announcing outgoing queue kill pill.");
        }
        this.incomingQueue.offer(new DeserializeKillPill());
    }

    public void digestAck(Digest digest) {
        DigestAck digestAck = new DigestAck();
        digestAck.setName( this.worker.getConfiguration().getClientName() );
        digestAck.setHeartbeat( Util.getTimestamp() );
        digestAck.setReply_id( digest.getId() );
        digestAck.setMembership(new ArrayList<DigestMember>());
        try {
            outgoingQueue.offer(new OutgoingData(OutgoingDataType.DIGEST, digestAck));
        } catch (GossiperlClientException ex) {
            LOG.error("[" + this.worker.getConfiguration().getClientName() + "] Received error when posting digestAck. Reason: ", ex);
        }
    }

    public void receive(DeserializeResult incoming) {
        this.incomingQueue.offer(incoming);
    }

    public void send(TBase digest) {
        try {
            this.outgoingQueue.offer(new OutgoingData(OutgoingDataType.DIGEST, digest));
        } catch (GossiperlClientException ex) {
            LOG.error("[" + this.worker.getConfiguration().getClientName() + "] Error while sending " + digest.getClass().getName() + ". Reason: ", ex);
        }
    }

    class OutgoingData {
        private OutgoingDataType type;
        private TBase digest;
        public OutgoingData( OutgoingDataType type ) throws GossiperlClientException {
            this(type, null);
        }
        public OutgoingData( OutgoingDataType type, TBase digest ) throws GossiperlClientException {
            this.type = type;
            this.digest = digest;
            if (this.digest == null && this.type == OutgoingDataType.DIGEST) {
                throw new GossiperlClientException("Outgoing data digest without digest!");
            }
        }
        public TBase getDigest() {
            return this.digest;
        }
        public OutgoingDataType getType() {
            return this.type;
        }
    }

    class OutgoingQueueWorker implements Runnable {
        public void run() {
            LOG.info("[" + worker.getConfiguration().getClientName() + "] Starting outgoing queue worker.");
            while (worker.isWorking()) {
                try {
                    OutgoingData data = outgoingQueue.take();
                    if ( data.type == OutgoingDataType.DIGEST ) {
                        TBase digest = data.getDigest();
                        LOG.debug("[" + worker.getConfiguration().getClientName() + "] Processing offered digest of type: " + digest.getClass().getName());
                        transport.send( digest );
                    } else if ( data.type == OutgoingDataType.KILL_PILL ) {
                        LOG.info("[" + worker.getConfiguration().getClientName() + "] Received request to stop outgoing queue processing. Stopping.");
                        break;
                    } else {
                        LOG.error("[" + worker.getConfiguration().getClientName() + "] Skipping unknown outgoing type: " + data.type );
                    }
                } catch (InterruptedException ex) {
                    LOG.error("[" + worker.getConfiguration().getClientName() + "] Error while loading data from the outgoing queue. Worker will stop. Reason: ", ex);
                    break;
                }
            }
        }
    }

    class IncomingQueueWorker implements Runnable {
        public void run() {
            LOG.info("[" + worker.getConfiguration().getClientName() + "] Starting incoming queue worker.");
            while (worker.isWorking()) {
                try {
                    DeserializeResult result = incomingQueue.take();
                    if (result instanceof DeserializeResultOK) {

                        String digestType = ((DeserializeResultOK)result).getDigestType();
                        if (digestType.equals(Serializer.DIGEST)) {
                            digestAck( (Digest)((DeserializeResultOK)result).getDigest() );
                        } else if (digestType.equals(Serializer.DIGEST_ACK)) {
                            worker.getState().receive( (DigestAck)((DeserializeResultOK)result).getDigest() );
                        } else if (digestType.equals(Serializer.DIGEST_EVENT)) {
                            // TODO: handle notification
                        } else if (digestType.equals(Serializer.DIGEST_SUBSCRIBE_ACK)) {
                            // TODO: handle notification
                        } else if (digestType.equals(Serializer.DIGEST_UNSUBSCRIBE_ACK)) {
                            // TODO: handle notification
                        } else if (digestType.equals(Serializer.DIGEST_FORWARDED_ACK)) {
                            // TODO: handle notification
                        } else {
                            // TODO: handle error
                        }

                    } else if ( result instanceof DeserializeResultError ) {
                        // TODO: handle incoming error

                        LOG.info("Incoming digest can't be deserialized" + ((DeserializeResultError)result).getCause());
                    } else if ( result instanceof DeserializeResultForward ) {
                        // TODO: handle forward message
                    } else if (result instanceof DeserializeKillPill) {
                        LOG.info("[" + worker.getConfiguration().getClientName() + "] Received request to stop incoming queue processing. Stopping.");
                        break;
                    } else {
                        LOG.error("[" + worker.getConfiguration().getClientName() + "] Skipping unknown incoming message: " + result.getClass().getName() );
                    }
                } catch (InterruptedException ex) {
                    LOG.error("[" + worker.getConfiguration().getClientName() + "] Error while loading data from the incoming queue. Worker will stop. Reason: ", ex);
                    break;
                }
            }
        }
    }

}
