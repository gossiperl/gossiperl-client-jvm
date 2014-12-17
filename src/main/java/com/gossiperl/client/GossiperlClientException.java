package com.gossiperl.client;

public class GossiperlClientException extends Exception {
    public GossiperlClientException() {}
    public GossiperlClientException(String message) { super(message); }
    public GossiperlClientException(Throwable cause) { super(cause); }
    public GossiperlClientException(String message, Throwable cause) { super(message, cause); }
}
