package com.gossiperl.client;

import com.gossiperl.client.config.SupervisorConfiguration;

public class Supervisor {

    public Supervisor() {
    }

    public void connect(SupervisorConfiguration config) {

    }

    public void connect(String overlayName, int overlayPort, int clientPort, String clientName, String clientSecret, String symmetricKey) {
        SupervisorConfiguration cfg = new SupervisorConfiguration();
        cfg.setOverlayName(overlayName);
        cfg.setOverlayPort(overlayPort);
        cfg.setClientPort(clientPort);
        cfg.setClientName(clientName);
        cfg.setClientSecret(clientSecret);
        cfg.setSymmetricKey(symmetricKey);
        this.connect( cfg );
    }

    public void disconnect(String overlayName) {

    }

    public String[] subscribe(String overlayName, String[] events) {
        return events;
    }

    public String[] unsubscribe(String overlayName, String[] events) {
        return events;
    }

    public State.Status getCurrentState(String overlayName) {
        return State.Status.DISCONNECTED;
    }

    public String[] getSubscriptions(String overlayName) {
        return new String[0];
    }

    public void stop() {

    }

    public static void main(String[] args) {

    }

}
