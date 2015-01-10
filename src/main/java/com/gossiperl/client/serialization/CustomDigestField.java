package com.gossiperl.client.serialization;

import com.gossiperl.client.exceptions.GossiperlClientException;
import com.gossiperl.client.exceptions.GossiperlUnsupportedSerializableTypeException;

public class CustomDigestField {

    private String fieldName;
    private Object value;
    private String type;
    private short fieldOrder;

    public CustomDigestField(String fieldName, Object value, String type, int fieldOrder)
            throws GossiperlUnsupportedSerializableTypeException, GossiperlClientException {
        if ( !Serializer.isSerializableType(type) ) {
            throw new GossiperlUnsupportedSerializableTypeException(type);
        }
        if ( fieldOrder < 0 || fieldOrder > Short.MAX_VALUE ) {
            throw new GossiperlClientException("Field ID must be at least 0 and no greater than " + Short.MAX_VALUE + ".");
        }
        this.fieldName = fieldName;
        this.value = value;
        this.type = type;
        this.fieldOrder = (short)fieldOrder;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public short getFieldOrder() {
        return fieldOrder;
    }

}
