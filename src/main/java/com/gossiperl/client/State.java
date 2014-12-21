package com.gossiperl.client;

import com.gossiperl.client.thrift.Digest;
import com.gossiperl.client.thrift.DigestAck;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class State {

    public enum Status {
        CONNECTED,
        DISCONNECTED
    }

    private OverlayWorker worker;
    private ArrayList<String> subscriptions;
    private Status currentStatus;
    private long lastTs;

    private static Logger LOG = Logger.getLogger(State.class);

    public State(OverlayWorker worker) {
        this.subscriptions = new ArrayList<String>();
        this.currentStatus = Status.DISCONNECTED;
        this.worker = worker;
        LOG.info("[" + this.worker.getConfiguration().getClientName() + "] State initialized.");
    }

    public void receive(DigestAck digestAck) {
        if ( this.currentStatus == Status.DISCONNECTED ) {
            this.worker.getListener().connected( this.worker );
            if ( this.subscriptions.size() > 0 ) {
                this.worker.getMessaging().subscribe(this.subscriptions);
            }
        }
        this.currentStatus = Status.CONNECTED;
        this.lastTs = digestAck.getHeartbeat();
    }

    public void start() {
        (new Thread(new StateTask())).start();
        LOG.info("[" + this.worker.getConfiguration().getClientName() + "] State task started.");
    }

    public List<String> subscribe(List<String> events) {
        this.worker.getMessaging().subscribe( events );
        this.subscriptions.addAll( events );
        return this.subscriptions;
    }

    public List<String> unsubscribe(List<String> events) {
        this.worker.getMessaging().unsubscribe(events);
        this.subscriptions.removeAll( events );
        return this.subscriptions;
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
                    if ( Util.getTimestamp() - lastTs > 5f ) {
                        LOG.debug("[" + worker.getConfiguration().getClientName() + "] Announcing disconnected.");
                        worker.getListener().disconnected(worker);
                        currentStatus = Status.DISCONNECTED;
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        LOG.error("[" + worker.getConfiguration().getClientName() + "] Error in state worker. State worker will stop. Reason: ", ex);
                        break;
                    }
                }
                LOG.debug("[" + worker.getConfiguration().getClientName() + "] Announcing disconnected. Worker stopped.");
                worker.getListener().disconnected( worker );
                currentStatus = Status.DISCONNECTED;
                LOG.info("[" + worker.getConfiguration().getClientName() + "] Stopping state service.");
            } catch (InterruptedException ex) {
                LOG.error("[" + worker.getConfiguration().getClientName() + "] Error while starting state worker. Reason: ", ex);
            }
        }
    }

}
