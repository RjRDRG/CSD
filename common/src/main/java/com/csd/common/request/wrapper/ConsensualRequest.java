package com.csd.common.request.wrapper;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;

public class ConsensualRequest<T extends Serializable> implements Serializable {

    private String requestId;
    private OffsetDateTime timestamp;
    private T request;
    private long lastTransactionId;

    public ConsensualRequest(String requestId, T request, long lastTransactionId) {
        this.requestId = requestId;
        this.timestamp = OffsetDateTime.now();
        this.request = request;
        this.lastTransactionId = lastTransactionId;
    }

    ConsensualRequest() {
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public T getRequest() {
        return request;
    }

    public void setRequest(T request) {
        this.request = request;
    }

    public long getLastTransactionId() {
        return lastTransactionId;
    }

    public void setLastTransactionId(long lastTransactionId) {
        this.lastTransactionId = lastTransactionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsensualRequest<?> that = (ConsensualRequest<?>) o;
        return lastTransactionId == that.lastTransactionId && requestId.equals(that.requestId) && timestamp.equals(that.timestamp) && request.equals(that.request);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, timestamp, request, lastTransactionId);
    }

    @Override
    public String toString() {
        return "ConsensualRequest{" +
                "requestId='" + requestId + '\'' +
                ", timestamp=" + timestamp +
                ", request=" + request +
                ", lastTransactionId=" + lastTransactionId +
                '}';
    }
}
