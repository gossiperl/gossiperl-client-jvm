package com.gossiperl.client.serialization;

import com.gossiperl.client.GossiperlClientException;
import com.gossiperl.client.thrift.DigestEnvelope;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TMemoryInputTransport;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.UUID;

public class Serializer {

    private HashMap<String, Class> types = new HashMap<String, Class>();

    public Serializer() {
        this.types.put("digestError", com.gossiperl.client.thrift.DigestError.class);
        this.types.put("digestForwardedAck", com.gossiperl.client.thrift.DigestForwardedAck.class);
        this.types.put("digestEnvelope", com.gossiperl.client.thrift.DigestEnvelope.class);
        this.types.put("digest", com.gossiperl.client.thrift.Digest.class);
        this.types.put("digestAck", com.gossiperl.client.thrift.DigestAck.class);
        this.types.put("digestSubscriptions", com.gossiperl.client.thrift.DigestSubscriptions.class);
        this.types.put("digestExit", com.gossiperl.client.thrift.DigestExit.class);
        this.types.put("digestSubscribe", com.gossiperl.client.thrift.DigestSubscribe.class);
        this.types.put("digestSubscribeAck", com.gossiperl.client.thrift.DigestSubscribeAck.class);
        this.types.put("digestUnsubscribe", com.gossiperl.client.thrift.DigestUnsubscribe.class);
        this.types.put("digestUnsubscribeAck", com.gossiperl.client.thrift.DigestUnsubscribeAck.class);
        this.types.put("digestEvent", com.gossiperl.client.thrift.DigestEvent.class);
    }

    public byte[] serialize(org.apache.thrift.TBase digest) throws GossiperlClientException,
            UnsupportedEncodingException {
        String digestType = this.getDigestName( digest );
        if ( digestType.equals("digestEnvelope") ) {
            return digestToBinary( digest );
        }
        DigestEnvelope envelope = new DigestEnvelope();
        envelope.setPayload_type( digestType );
        envelope.setBin_payload( new String( digestToBinary( digest ) ) );
        envelope.setId(UUID.randomUUID().toString());
        return digestToBinary( envelope );
    }

    public DeserializeResult deserialize(byte[] binDigest) throws GossiperlClientException,
            UnsupportedEncodingException {
        DigestEnvelope envelope = (DigestEnvelope)digestFromBinary( "digestEnvelope", binDigest );
        if ( this.types.containsKey(envelope.getPayload_type()) ) {
            try {
                TBase digest = digestFromBinary(envelope.getPayload_type(), envelope.getBin_payload().getBytes());
                return new DeserializeResultOK(envelope.getPayload_type(), digest);
            } catch (GossiperlClientException ex) {
                return new DeserializeResultError(ex);
            }
        } else {
            return new DeserializeResultForward( envelope.getPayload_type(), envelope );
        }
    }

    public byte[] digestToBinary(org.apache.thrift.TBase digest) throws GossiperlClientException {
        try {
            return (new TSerializer()).serialize( digest );
        } catch (TException ex) {
            throw new GossiperlClientException("Could not write Thrift digest.", ex);
        }
    }

    public TBase digestFromBinary(String digestType, byte[] binDigest) throws GossiperlClientException {
        TMemoryInputTransport transport = new TMemoryInputTransport( binDigest );
        TBinaryProtocol protocol = new TBinaryProtocol( transport );
        if (this.types.containsKey( digestType )) {
            Class cls = this.types.get( digestType );
            try {
                TBase digest = (TBase) (cls.newInstance());
                digest.read(protocol);
                return digest;
            } catch (InstantiationException ex) {
                throw new GossiperlClientException("Could not create Thrift digest class instance.", ex);
            } catch (IllegalAccessException ex) {
                throw new GossiperlClientException("Could not load Thrift digest class.", ex);
            } catch (TException ex) {
                ex.printStackTrace();
                throw new GossiperlClientException("Could not read Thrift digest.", ex);
            }
        } else {
            throw new GossiperlClientException("Digest type " + digestType + " unknown");
        }
    }

    private String getDigestName(TBase digest) {
        String clsName = digest.getClass().getName();
        clsName = clsName.substring( clsName.lastIndexOf(".")+1, clsName.length() );
        return clsName.substring(0,1).toLowerCase() + clsName.substring(1, clsName.length());
    }

}
