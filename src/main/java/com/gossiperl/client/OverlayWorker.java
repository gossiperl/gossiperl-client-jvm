package com.gossiperl.client;

import com.gossiperl.client.config.OverlayConfiguration;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class OverlayWorker {

    private Supervisor supervisor;
    private OverlayConfiguration configuration;
    private Messaging messaging;
    private State state;
    private boolean working;

    private static Logger LOG = Logger.getLogger(OverlayWorker.class);

    public OverlayWorker(Supervisor supervisor, OverlayConfiguration configuration) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        this.supervisor = supervisor;
        this.configuration = configuration;
        this.messaging = new Messaging(this);
        this.state = new State(this);
        this.working = true;
        LOG.info("[" + this.configuration.getClientName() + "] Overlay worker initialized.");
    }

    public OverlayConfiguration getConfiguration() {
        return this.configuration;
    }

    public boolean isWorking() {
        return this.working;
    }

    public void start() {
        Thread t = new Thread(new OverlayWorkerTask());
        t.start();
        LOG.info("[" + this.configuration.getClientName() + "] Overlay worker started.");
        try {
            t.join();
        } catch (InterruptedException ex) {
            LOG.error("[" + this.configuration.getClientName() + "] Error while starting overlay worker. Reason: ", ex);
        }
    }

    public void stop() {

    }

    public State getState() {
        return this.state;
    }

    public Messaging getMessaging() {
        return this.messaging;
    }

    class OverlayWorkerTask implements Runnable {
        public void run() {
            messaging.start();
            state.start();
        }
    }

}
