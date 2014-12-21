package com.gossiperl.client.serialization;

import com.gossiperl.client.exceptions.GossiperlUnsupportedSerializableTypeException;

public class CustomDigestField {

    private String fieldName;
    private Object value;
    private String type;
    private short fieldOrder;

    public CustomDigestField(String fieldName, Object value, String type, short fieldOrder)
            throws GossiperlUnsupportedSerializableTypeException {
        if ( !Serializer.isSerializableType(type) ) {
            throw new GossiperlUnsupportedSerializableTypeException(type);
        }
        this.fieldName = fieldName;
        this.value = value;
        this.type = type;
        this.fieldOrder = fieldOrder;
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
