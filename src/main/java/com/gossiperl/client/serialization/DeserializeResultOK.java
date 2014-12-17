package com.gossiperl.client.serialization;

import org.apache.thrift.TBase;

public class DeserializeResultOK extends DeserializeResult {
    private String digestType;
    private TBase digest;
    public DeserializeResultOK(String digestType, TBase digest) {
        this.digestType = digestType;
        this.digest = digest;
    }
    public String getDigestType() {
        return this.digestType;
    }
    public TBase getDigest() {
        return this.digest;
    }
}
