package com.gossiperl.client;

import com.gossiperl.client.exceptions.GossiperlClientException;
import com.gossiperl.client.serialization.*;
import com.gossiperl.client.thrift.*;
import com.gossiperl.client.transport.Udp;
import org.apache.log4j.Logger;
import org.apache.thrift.TBase;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

public class Messaging {

    public enum OutgoingDataType {
        ARBITRARY,
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

    protected void digestAck(Digest digest) {
        DigestAck digestAck = new DigestAck();
        digestAck.setName( this.worker.getConfiguration().getClientName() );
        digestAck.setHeartbeat( Util.getTimestamp() );
        digestAck.setReply_id( digest.getId() );
        digestAck.setMembership(new ArrayList<DigestMember>());
        send( digestAck );
    }

    protected void digestForwardedAck( String digestId ) {
        DigestForwardedAck ack = new DigestForwardedAck();
        ack.setReply_id( digestId );
        ack.setName( this.worker.getConfiguration().getClientName() );
        ack.setSecret(this.worker.getConfiguration().getClientSecret());
        send( ack );
    }

    protected boolean digestExit() {
        DigestExit digest = new DigestExit();
        digest.setName( worker.getConfiguration().getClientName() );
        digest.setSecret( worker.getConfiguration().getClientSecret() );
        digest.setHeartbeat( Util.getTimestamp() );
        send( digest );
        return true;
    }

    protected void digestSubscribe(List<String> events) {
        DigestSubscribe digest = new DigestSubscribe();
        digest.setName(this.worker.getConfiguration().getClientName());
        digest.setSecret(this.worker.getConfiguration().getClientSecret());
        digest.setHeartbeat(Util.getTimestamp());
        digest.setId(UUID.randomUUID().toString());
        digest.setEvent_types( events );
        send( digest );
    }

    protected void digestUnsubscribe(List<String> events) {
        DigestUnsubscribe digest = new DigestUnsubscribe();
        digest.setName(this.worker.getConfiguration().getClientName());
        digest.setSecret(this.worker.getConfiguration().getClientSecret());
        digest.setHeartbeat(Util.getTimestamp());
        digest.setId(UUID.randomUUID().toString());
        digest.setEvent_types( events );
        send( digest );
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

    public void send(String digestType, List<CustomDigestField> digest) {
        try {
            this.outgoingQueue.offer(new OutgoingData(OutgoingDataType.ARBITRARY, digestType, digest));
        } catch (GossiperlClientException ex) {
            LOG.error("[" + this.worker.getConfiguration().getClientName() + "] Error while sending " + digest.getClass().getName() + ". Reason: ", ex);
        }
    }

    class OutgoingData {
        private OutgoingDataType type;
        private TBase digest;
        private List<CustomDigestField> arbitraryData;
        private String arbitraryType;
        public OutgoingData( OutgoingDataType type ) throws GossiperlClientException {
            if (type != OutgoingDataType.KILL_PILL) {
                throw new GossiperlClientException("Single argument constructor valid only for KILL_PILL.");
            }
            this.type = type;
        }
        public OutgoingData( OutgoingDataType type, TBase digest ) throws GossiperlClientException {
            this.type = type;
            this.digest = digest;
            if (this.digest == null && this.type == OutgoingDataType.DIGEST) {
                throw new GossiperlClientException("Outgoing data digest without digest!");
            }
        }
        public OutgoingData( OutgoingDataType type, String digestType, List<CustomDigestField> digest ) throws GossiperlClientException {
            this.type = type;
            this.arbitraryType = digestType;
            this.arbitraryData = digest;
            if (this.digest == null && this.type == OutgoingDataType.ARBITRARY) {
                throw new GossiperlClientException("Outgoing data digest without digest!");
            }
        }
        public String getDigestType() {
            if ( this.type == OutgoingDataType.ARBITRARY ) {
                return this.arbitraryType;
            } else if ( this.type == OutgoingDataType.DIGEST ) {
                return this.digest.getClass().getName();
            }
            return null;
        }
        public List<CustomDigestField> getDigestData() {
            return this.arbitraryData;
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
                        transport.send(digest);
                    } else if ( data.type == OutgoingDataType.ARBITRARY ) {
                        transport.send( data.getDigestType(), data.getDigestData() );
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
                            DigestEvent event = (DigestEvent)((DeserializeResultOK)result).getDigest();
                            worker.getListener().event( worker, event.getEvent_type(), event.getEvent_object(), event.getHeartbeat() );
                        } else if (digestType.equals(Serializer.DIGEST_SUBSCRIBE_ACK)) {
                            DigestSubscribeAck event = (DigestSubscribeAck)((DeserializeResultOK)result).getDigest();
                            worker.getListener().subscribeAck(worker, event.getEvent_types());
                        } else if (digestType.equals(Serializer.DIGEST_UNSUBSCRIBE_ACK)) {
                            DigestUnsubscribeAck event = (DigestUnsubscribeAck)((DeserializeResultOK)result).getDigest();
                            worker.getListener().unsubscribeAck(worker, event.getEvent_types());
                        } else if (digestType.equals(Serializer.DIGEST_FORWARDED_ACK)) {
                            DigestForwardedAck event = (DigestForwardedAck)((DeserializeResultOK)result).getDigest();
                            worker.getListener().forwardAck(worker, event.getReply_id());
                        } else {
                            worker.getListener().failed(worker, new GossiperlClientException("Unknown digest type " + ((DeserializeResultOK)result).getDigestType() ));
                        }

                    } else if ( result instanceof DeserializeResultError ) {
                        worker.getListener().failed(worker, ((DeserializeResultError) result).getCause());
                    } else if ( result instanceof DeserializeResultForward ) {
                        DeserializeResultForward event = (DeserializeResultForward)result;
                        worker.getListener().forwarded( worker, event.getDigestType(), event.getDigest() );
                        digestForwardedAck(event.getEnvelopeId());
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
