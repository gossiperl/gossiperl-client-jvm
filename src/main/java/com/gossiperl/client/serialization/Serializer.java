package com.gossiperl.client.serialization;

import com.gossiperl.client.exceptions.GossiperlClientException;
import com.gossiperl.client.thrift.DigestEnvelope;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.TMemoryInputTransport;
import org.apache.thrift.transport.TTransport;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Serializer {

    private HashMap<String, Class> types = new HashMap<String, Class>();

    private static HashMap<String, Byte> serializableTypes = new HashMap<String, Byte>();

    public static final String DIGEST_ERROR = "digestError";
    public static final String DIGEST_FORWARDED_ACK = "digestForwardedAck";
    public static final String DIGEST_ENVELOPE = "digestEnvelope";
    public static final String DIGEST = "digest";
    public static final String DIGEST_ACK = "digestAck";
    public static final String DIGEST_SUBSCRIPTIONS = "digestSubscriptions";
    public static final String DIGEST_EXIT = "digestExit";
    public static final String DIGEST_SUBSCRIBE = "digestSubscribe";
    public static final String DIGEST_SUBSCRIBE_ACK = "digestSubscribeAck";
    public static final String DIGEST_UNSUBSCRIBE = "digestUnsubscribe";
    public static final String DIGEST_UNSUBSCRIBE_ACK = "digestUnsubscribeAck";
    public static final String DIGEST_EVENT = "digestEvent";

    public Serializer() {
        ensureSerializableTypes();
        this.types.put(DIGEST_ERROR, com.gossiperl.client.thrift.DigestError.class);
        this.types.put(DIGEST_FORWARDED_ACK, com.gossiperl.client.thrift.DigestForwardedAck.class);
        this.types.put(DIGEST_ENVELOPE, com.gossiperl.client.thrift.DigestEnvelope.class);
        this.types.put(DIGEST, com.gossiperl.client.thrift.Digest.class);
        this.types.put(DIGEST_ACK, com.gossiperl.client.thrift.DigestAck.class);
        this.types.put(DIGEST_SUBSCRIPTIONS, com.gossiperl.client.thrift.DigestSubscriptions.class);
        this.types.put(DIGEST_EXIT, com.gossiperl.client.thrift.DigestExit.class);
        this.types.put(DIGEST_SUBSCRIBE, com.gossiperl.client.thrift.DigestSubscribe.class);
        this.types.put(DIGEST_SUBSCRIBE_ACK, com.gossiperl.client.thrift.DigestSubscribeAck.class);
        this.types.put(DIGEST_UNSUBSCRIBE, com.gossiperl.client.thrift.DigestUnsubscribe.class);
        this.types.put(DIGEST_UNSUBSCRIBE_ACK, com.gossiperl.client.thrift.DigestUnsubscribeAck.class);
        this.types.put(DIGEST_EVENT, com.gossiperl.client.thrift.DigestEvent.class);


    }

    private static void ensureSerializableTypes() {
        if (serializableTypes.size() == 0) {
            serializableTypes.put( "string", TType.STRING );
            serializableTypes.put( "bool", TType.BOOL );
            serializableTypes.put( "byte", TType.BYTE );
            serializableTypes.put( "double", TType.DOUBLE );
            serializableTypes.put( "i16", TType.I16 );
            serializableTypes.put( "i32", TType.I32 );
            serializableTypes.put( "i64", TType.I64 );
        }
    }

    public static boolean isSerializableType(String type) {
        ensureSerializableTypes();
        return serializableTypes.containsKey(type);
    }

    public TBase serializeArbitrary(String digestType, List<CustomDigestField> digestData) throws TException, GossiperlClientException {
        TTransport transport = new TMemoryInputTransport();
        TProtocol  protocol  = new TBinaryProtocol(transport);
        protocol.writeStructBegin(new TStruct( digestType ));
        for (CustomDigestField field : digestData) {
            byte fieldType = serializableTypes.get(field.getType());
            protocol.writeFieldBegin( new TField( field.getFieldName(), fieldType, field.getFieldOrder() ));
            if ( fieldType == TType.BOOL ) {
                try {
                    protocol.writeBool( ((Boolean)field.getValue()).booleanValue() );
                } catch (Exception ex) {
                    throw new GossiperlClientException("Failed to write value " + field.getValue().toString() + " as BOOL.", ex);
                }
            } else if ( fieldType == TType.BYTE ) {
                try {
                    protocol.writeByte(((Byte) field.getValue()).byteValue());
                } catch (Exception ex) {
                    throw new GossiperlClientException("Failed to write value " + field.getValue().toString() + " as BYTE.", ex);
                }
            } else if ( fieldType == TType.DOUBLE ) {
                try {
                    protocol.writeDouble(((Double) field.getValue()).doubleValue());
                } catch (Exception ex) {
                    throw new GossiperlClientException("Failed to write value " + field.getValue().toString() + " as DOUBLE.", ex);
                }
            } else if ( fieldType == TType.I16 ) {
                try {
                    protocol.writeI16(((Long) field.getValue()).shortValue());
                } catch (Exception ex) {
                    throw new GossiperlClientException("Failed to write value " + field.getValue().toString() + " as I16.", ex);
                }
            } else if ( fieldType == TType.I32 ) {
                try {
                    protocol.writeI32(((Long) field.getValue()).intValue());
                } catch (Exception ex) {
                    throw new GossiperlClientException("Failed to write value " + field.getValue().toString() + " as I32.", ex);
                }
            } else if ( fieldType == TType.I64 ) {
                try {
                    protocol.writeI64(((Long)field.getValue()).longValue());
                } catch (Exception ex) {
                    throw new GossiperlClientException("Failed to write value " + field.getValue().toString() + " as I64.", ex);
                }
            } else if ( fieldType == TType.STRING ) {
                try {
                    protocol.writeString((String) field.getValue());
                } catch (Exception ex) {
                    throw new GossiperlClientException("Failed to write value " + field.getValue().toString() + " as STRING.", ex);
                }
            }
            protocol.writeFieldEnd();
        }
        protocol.writeFieldStop();
        protocol.writeStructEnd();
        DigestEnvelope envelope = new DigestEnvelope();
        envelope.setPayload_type( digestType );
        envelope.setBin_payload(Base64.encode( protocol.getTransport().getBuffer() ));
        envelope.setId(UUID.randomUUID().toString());
        return envelope;
    }

    public byte[] serialize(org.apache.thrift.TBase digest) throws GossiperlClientException,
            UnsupportedEncodingException {
        String digestType = this.getDigestName( digest );
        if ( digestType.equals(DIGEST_ENVELOPE) ) {
            return digestToBinary( digest );
        }
        DigestEnvelope envelope = new DigestEnvelope();
        envelope.setPayload_type( digestType );
        envelope.setBin_payload(Base64.encode(digestToBinary(digest)));
        envelope.setId(UUID.randomUUID().toString());
        return digestToBinary( envelope );
    }

    public DeserializeResult deserialize(byte[] binDigest) throws GossiperlClientException,
            UnsupportedEncodingException {
        DigestEnvelope envelope = (DigestEnvelope)digestFromBinary( DIGEST_ENVELOPE, binDigest );
        if ( this.types.containsKey(envelope.getPayload_type()) ) {
            try {
                TBase digest = digestFromBinary(envelope.getPayload_type(), Base64.decode(envelope.getBin_payload()));
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
            return (new TSerializer()).serialize(digest);
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
