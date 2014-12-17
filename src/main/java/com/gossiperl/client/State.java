package com.gossiperl.client;

public class State {

    public enum Status {
        CONNECTED,
        DISCONNECTED
    }

    private OverlayWorker worker;

    public State(OverlayWorker worker) {
        // TODO: implement
        this.worker = worker;
    }

}
