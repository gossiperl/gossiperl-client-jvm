package com.gossiperl.client.listener;

import com.gossiperl.client.GossiperlClientException;
import com.gossiperl.client.OverlayWorker;
import org.apache.log4j.Logger;
import org.apache.thrift.TBase;

public class DefaultGossiperlClientListener implements GossiperlClientListener {

    private static Logger LOG = Logger.getLogger(DefaultGossiperlClientListener.class);

    public void connected(OverlayWorker worker) {
        LOG.info("[" + worker.getConfiguration().getClientName() + "] Connected.");
    }

    public void disconnected(OverlayWorker worker) {
        LOG.info("[" + worker.getConfiguration().getClientName() + "] Disconnected.");
    }

    public void event(OverlayWorker worker, String eventType, Object member, long heartbeat) {
        LOG.info("[" + worker.getConfiguration().getClientName() + "] Received member " + member.toString() + " event " + eventType + " at " + heartbeat + ".");
    }

    public void subscribeAck( OverlayWorker worker, String[] eventTypes ) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<eventTypes.length; i++) {
            if (i > 0) {
                sb.append( ", " );
            }
            sb.append(eventTypes[i]);
        }
        LOG.info("[" + worker.getConfiguration().getClientName() + "] Subscribed to " + sb.toString() + ".");
    }

    public void unsubscribeAck( OverlayWorker worker, String[] eventTypes ) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<eventTypes.length; i++) {
            if (i > 0) {
                sb.append( ", " );
            }
            sb.append(eventTypes[i]);
        }
        LOG.info("[" + worker.getConfiguration().getClientName() + "] Unsubscribed from " + sb.toString() + ".");
    }

    public void forwardAck(OverlayWorker worker, String reply_id) {
        LOG.info("[" + worker.getConfiguration().getClientName() + "] Received confirmation of a forward message. Message ID: " + reply_id + ".");
    }

    public void forwarded(OverlayWorker worker, String digestType, TBase digest) {
        LOG.info("[" + worker.getConfiguration().getClientName() + "] Received forward digest " + digestType + ". Class of the digest: " + digest.getClass().getName() + ".");
    }

    public void failed(OverlayWorker worker, GossiperlClientException exception) {
        LOG.info("[" + worker.getConfiguration().getClientName() + "] Encountered an error: " + exception.getMessage() + ".");
    }

}
