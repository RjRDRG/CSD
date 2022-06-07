package com.csd.common.item;

import java.io.Serializable;
import java.time.OffsetDateTime;

import static com.csd.common.util.Serialization.dataToJson;

public class TransactionDetails implements Serializable {
    private OffsetDateTime timestamp;

    public TransactionDetails(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public TransactionDetails() {
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
