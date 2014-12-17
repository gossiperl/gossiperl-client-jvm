package com.gossiperl.client;

import com.gossiperl.client.config.OverlayConfiguration;

public class OverlayWorker {

    private OverlayConfiguration configuration;
    private Messaging messaging;
    private State state;
    private boolean working;

    public OverlayWorker(OverlayConfiguration configuration) {
        this.configuration = configuration;
        this.messaging = new Messaging(this);
        this.state = new State(this);
        this.working = true;
        // TODO: implement
    }

    public OverlayConfiguration getConfiguration() {
        return this.configuration;
    }

    public boolean isWorking() {
        return this.working;
    }

}
