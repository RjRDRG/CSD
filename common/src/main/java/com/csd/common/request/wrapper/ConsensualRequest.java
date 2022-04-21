package com.csd.common.request.wrapper;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;

import static com.csd.common.util.Serialization.bytesToData;

public class ConsensualRequest implements Serializable {

    private OffsetDateTime timestamp;
    private LedgerOperation operation;
    private byte[] encodedRequest;
    private long lastEntryId;

    public ConsensualRequest(LedgerOperation operation, byte[] encodedRequest, long lastEntryId) {
        this.timestamp = OffsetDateTime.now();
        this.operation = operation;
        this.encodedRequest = encodedRequest;
        this.lastEntryId = lastEntryId;
    }

    ConsensualRequest() {
    }

    public <T extends Serializable> T extractRequest() {
        return bytesToData(encodedRequest);
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LedgerOperation getOperation() {
        return operation;
    }

    public void setOperation(LedgerOperation operation) {
        this.operation = operation;
    }

    public byte[] getEncodedRequest() {
        return encodedRequest;
    }

    public void setEncodedRequest(byte[] encodedRequest) {
        this.encodedRequest = encodedRequest;
    }

    public long getLastEntryId() {
        return lastEntryId;
    }

    public void setLastEntryId(long lastEntryId) {
        this.lastEntryId = lastEntryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsensualRequest that = (ConsensualRequest) o;
        return lastEntryId == that.lastEntryId && timestamp.equals(that.timestamp) && operation == that.operation && Arrays.equals(encodedRequest, that.encodedRequest);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(timestamp, operation, lastEntryId);
        result = 31 * result + Arrays.hashCode(encodedRequest);
        return result;
    }

    @Override
    public String toString() {
        return "ReplicatedRequest{" +
                ", timestamp=" + timestamp +
                ", operation=" + operation +
                ", encodedRequest=" + Arrays.toString(encodedRequest) +
                ", lastEntryId=" + lastEntryId +
                '}';
    }

    public enum LedgerOperation implements Serializable {
        LOAD, BALANCE
    }
}
