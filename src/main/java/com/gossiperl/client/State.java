package com.gossiperl.client;

import com.gossiperl.client.thrift.Digest;
import com.gossiperl.client.thrift.DigestAck;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.UUID;

public class State {

    public enum Status {
        CONNECTED,
        DISCONNECTED
    }

    private OverlayWorker worker;
    private ArrayList<String> subscriptions;
    private Status currentStatus;
    private int lastTs;

    private static Logger LOG = Logger.getLogger(State.class);

    public State(OverlayWorker worker) {
        this.subscriptions = new ArrayList<String>();
        this.currentStatus = Status.DISCONNECTED;
        this.worker = worker;
        LOG.info("[" + this.worker.getConfiguration().getClientName() + "] State initialized.");
    }

    public void receive(DigestAck digestAck) {
        // TODO: implement
    }

    public void start() {
        (new Thread(new StateTask())).start();
        LOG.info("[" + this.worker.getConfiguration().getClientName() + "] State task started.");
    }

    public String[] subscribe(String[] events) {
        ArrayList<String> toAdd = new ArrayList<String>();
        for (int i=0; i<events.length; i++) {
            toAdd.add(events[i]);
        }
        this.subscriptions.addAll( toAdd );
        // TODO: implement
        return events;
    }

    public String[] unsubscribe(String[] events) {
        ArrayList<String> toRemove = new ArrayList<String>();
        for (int i=0; i<events.length; i++) {
            toRemove.add(events[i]);
        }
        this.subscriptions.removeAll( toRemove );
        // TODO: implement
        return events;
    }

    public Status getCurrentState() {
        return this.currentStatus;
    }

    public String[] getSubscriptions() {
        return (String[])this.subscriptions.toArray();
    }

    private void sendDigest() {
        Digest digest = new Digest();
        digest.setSecret( this.worker.getConfiguration().getClientSecret() );
        digest.setId(UUID.randomUUID().toString());
        digest.setHeartbeat(Util.getTimestamp());
        digest.setPort(this.worker.getConfiguration().getClientPort());
        digest.setName(this.worker.getConfiguration().getClientName());
        LOG.debug("[" + this.worker.getConfiguration().getClientName() + "] Offering digest " + digest.getId() + ".");
        this.worker.getMessaging().send( digest );
    }

    class StateTask implements Runnable {
        public void run() {
            try {
                Thread.sleep(1000);
                while (worker.isWorking()) {
                    sendDigest();
                    // TODO: implement
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        LOG.error("[" + worker.getConfiguration().getClientName() + "] Error in state worker. State worker will stop. Reason: ", ex);
                        break;
                    }
                }
            } catch (InterruptedException ex) {
                LOG.error("[" + worker.getConfiguration().getClientName() + "] Error while starting state worker. Reason: ", ex);
            }
        }
    }

}
