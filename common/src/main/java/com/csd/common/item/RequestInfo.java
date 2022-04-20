package com.csd.common.item;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;

import static com.csd.common.util.Serialization.dataToJson;

public class RequestInfo implements Serializable {
    private String id;
    private Map<String,String> others;
    private OffsetDateTime timestamp;

    public RequestInfo(String id, OffsetDateTime timestamp) {
        this.id = id;
        this.others = null;
        this.timestamp = timestamp;
    }

    public RequestInfo(String id, Map<String,String> others, OffsetDateTime timestamp) {
        this.id = id;
        this.others = others;
        this.timestamp = timestamp;
    }

    public RequestInfo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getOthers() {
        return others;
    }

    public void setOthers(Map<String, String> others) {
        this.others = others;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "\nRequestInfo " + dataToJson(this);
    }
}
