package com.gossiperl.client.serialization;

public class CustomDigestField {

    private String fieldName;
    private Object value;
    private int fieldId;

    public CustomDigestField(String fieldName, Object value, int fieldId) {
        this.fieldName = fieldName;
        this.fieldId = fieldId;
        this.value = value;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getValue() {
        return value;
    }

    public int getFieldId() {
        return fieldId;
    }
}
