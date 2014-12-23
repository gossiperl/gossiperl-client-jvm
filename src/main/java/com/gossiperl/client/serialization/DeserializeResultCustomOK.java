package com.gossiperl.client.serialization;

import java.util.Map;

public class DeserializeResultCustomOK extends DeserializeResult {
    private String digestType;
    private Map<String, Object> resultData;
    public DeserializeResultCustomOK(String digestType, Map<String, Object> resultData) {
        this.digestType = digestType;
        this.resultData = resultData;
    }
    public String getDigestType() {
        return this.digestType;
    }
    public Map<String, Object> getResultData() {
        return this.resultData;
    }
}
