package com.gossiperl.client;

import com.gossiperl.client.config.OverlayConfiguration;

public class Supervisor {

    public Supervisor() {
    }

    public void connect(OverlayConfiguration config) {
        // TODO: implement
    }

    public void connect(String overlayName, int overlayPort, int clientPort, String clientName, String clientSecret, String symmetricKey) {
        OverlayConfiguration cfg = new OverlayConfiguration();
        cfg.setOverlayName(overlayName);
        cfg.setOverlayPort(overlayPort);
        cfg.setClientPort(clientPort);
        cfg.setClientName(clientName);
        cfg.setClientSecret(clientSecret);
        cfg.setSymmetricKey(symmetricKey);
        this.connect( cfg );
    }

    public void disconnect(String overlayName) {
        // TODO: implement
    }

    public String[] subscribe(String overlayName, String[] events) {
        // TODO: implement
        return events;
    }

    public String[] unsubscribe(String overlayName, String[] events) {
        // TODO: implement
        return events;
    }

    public State.Status getCurrentState(String overlayName) {
        // TODO: implement
        return State.Status.DISCONNECTED;
    }

    public String[] getSubscriptions(String overlayName) {
        // TODO: implement
        return new String[0];
    }

    public void stop() {
        // TODO: implement
    }

    public static void main(String[] args) {

    }

}
