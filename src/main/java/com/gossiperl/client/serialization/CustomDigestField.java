package com.gossiperl.client.serialization;

import com.gossiperl.client.exceptions.GossiperlClientException;
import org.apache.thrift.protocol.TType;

public class CustomDigestField {

    private String fieldName;
    private Object value;
    private byte type;
    private short fieldOrder;

    public CustomDigestField(String fieldName, String value, int fieldOrder)
        throws GossiperlClientException {
        if ( fieldOrder < 0 || fieldOrder > Short.MAX_VALUE ) {
            throw new GossiperlClientException("Field ID must be at least 0 and no greater than " + Short.MAX_VALUE + ".");
        }
        this.fieldName = fieldName;
        this.fieldOrder = (short)fieldOrder;
        this.value = value;
        this.type = TType.STRING;
    }

    public CustomDigestField(String fieldName, boolean value, int fieldOrder)
        throws GossiperlClientException {
        if ( fieldOrder < 0 || fieldOrder > Short.MAX_VALUE ) {
            throw new GossiperlClientException("Field ID must be at least 0 and no greater than " + Short.MAX_VALUE + ".");
        }
        this.fieldName = fieldName;
        this.fieldOrder = (short)fieldOrder;
        this.value = new Boolean(value);
        this.type = TType.BOOL;
    }

    public CustomDigestField(String fieldName, byte value, int fieldOrder)
        throws GossiperlClientException {
        if ( fieldOrder < 0 || fieldOrder > Short.MAX_VALUE ) {
            throw new GossiperlClientException("Field ID must be at least 0 and no greater than " + Short.MAX_VALUE + ".");
        }
        this.fieldName = fieldName;
        this.fieldOrder = (short)fieldOrder;
        this.value = new Byte(value);
        this.type = TType.BYTE;
    }

    public CustomDigestField(String fieldName, double value, int fieldOrder)
        throws GossiperlClientException {
        if ( fieldOrder < 0 || fieldOrder > Short.MAX_VALUE ) {
            throw new GossiperlClientException("Field ID must be at least 0 and no greater than " + Short.MAX_VALUE + ".");
        }
        this.fieldName = fieldName;
        this.fieldOrder = (short)fieldOrder;
        this.value = new Double(value);
        this.type = TType.DOUBLE;
    }

    public CustomDigestField(String fieldName, short value, int fieldOrder)
        throws GossiperlClientException {
        if ( fieldOrder < 0 || fieldOrder > Short.MAX_VALUE ) {
            throw new GossiperlClientException("Field ID must be at least 0 and no greater than " + Short.MAX_VALUE + ".");
        }
        this.fieldName = fieldName;
        this.fieldOrder = (short)fieldOrder;
        this.value = new Short(value);
        this.type = TType.I16;
    }

    public CustomDigestField(String fieldName, int value, int fieldOrder)
        throws GossiperlClientException {
        if ( fieldOrder < 0 || fieldOrder > Short.MAX_VALUE ) {
            throw new GossiperlClientException("Field ID must be at least 0 and no greater than " + Short.MAX_VALUE + ".");
        }
        this.fieldName = fieldName;
        this.fieldOrder = (short)fieldOrder;
        this.value = new Integer(value);
        this.type = TType.I32;
    }

    public CustomDigestField(String fieldName, long value, int fieldOrder)
        throws GossiperlClientException {
        if ( fieldOrder < 0 || fieldOrder > Short.MAX_VALUE ) {
            throw new GossiperlClientException("Field ID must be at least 0 and no greater than " + Short.MAX_VALUE + ".");
        }
        this.fieldName = fieldName;
        this.fieldOrder = (short)fieldOrder;
        this.value = new Long(value);
        this.type = TType.I64;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getValue() {
        return value;
    }

    public byte getType() {
        return type;
    }

    public short getFieldOrder() {
        return fieldOrder;
    }

}
