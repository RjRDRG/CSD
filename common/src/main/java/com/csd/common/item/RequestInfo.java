package com.csd.common.item;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;

import static com.csd.common.util.Serialization.dataToJson;

public class RequestInfo implements Serializable {
    private OffsetDateTime timestamp;

    public RequestInfo(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public RequestInfo() {
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return dataToJson(this);
    }
}
