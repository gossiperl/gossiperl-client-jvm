package com.gossiperl.client.serialization;

import com.gossiperl.client.exceptions.GossiperlClientException;

public class DeserializeResultError extends DeserializeResult {
    private GossiperlClientException cause;
    public DeserializeResultError(GossiperlClientException ex) {
        this.cause = ex;
    }
    public GossiperlClientException getCause() {
        return this.cause;
    }
}
