package com.gossiperl.client.serialization;

import com.gossiperl.client.thrift.DigestEnvelope;
import org.apache.thrift.TBase;

public class DeserializeResultForward extends DeserializeResultOK {
    public DeserializeResultForward(String digestType, DigestEnvelope digest) {
        super(digestType, digest);
    }
    public String getEnvelopeId() {
        return ((DigestEnvelope)this.getDigest()).getId();
    }
    public DigestEnvelope getEnvelope() {
        return (DigestEnvelope)this.getDigest();
    }
}
