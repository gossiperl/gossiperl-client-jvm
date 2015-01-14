package com.gossiperl.client.config;

/**
 * Created by rad on 16/12/14.
 */
public class OverlayConfiguration {

    private String overlayName;
    private String clientName;
    private String clientSecret;
    private String symmetricKey;
    private int overlayPort;
    private int clientPort;
    private String log4jConfiguration = null;

    public String getOverlayName() {
        return overlayName;
    }

    public void setOverlayName(String overlayName) {
        this.overlayName = overlayName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getSymmetricKey() {
        return symmetricKey;
    }

    public void setSymmetricKey(String symmetricKey) {
        this.symmetricKey = symmetricKey;
    }

    public int getOverlayPort() {
        return overlayPort;
    }

    public void setOverlayPort(int overlayPort) {
        this.overlayPort = overlayPort;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public String getLog4jConfiguration() {
        return log4jConfiguration;
    }

    public void setLog4jConfiguration(String log4jConfiguration) {
        this.log4jConfiguration = log4jConfiguration;
    }

}
