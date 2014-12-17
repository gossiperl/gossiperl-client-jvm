package com.gossiperl.client.transport;

public class TransportMessage {
    private byte[] data;
    public TransportMessage(byte[] data) {
        this.data = data;
    }
    public byte[] getData() {
        return this.data;
    }
}
