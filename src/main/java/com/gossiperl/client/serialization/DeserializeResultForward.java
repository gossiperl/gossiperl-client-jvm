package com.gossiperl.client.serialization;

import com.gossiperl.client.thrift.DigestEnvelope;
import org.apache.thrift.TBase;

public class DeserializeResultForward extends DeserializeResult {

    private String digestType;
    private byte[] binaryEnvelope;
    private String envelopeId;

    public DeserializeResultForward(String digestType, byte[] binaryEnvelope, String envelopeId) {
        this.digestType = digestType;
        this.binaryEnvelope = binaryEnvelope;
        this.envelopeId = envelopeId;
    }

    public String getDigestType() {
        return digestType;
    }

    public byte[] getBinaryEnvelope() {
        return binaryEnvelope;
    }

    public String getEnvelopeId() {
        return envelopeId;
    }
}
