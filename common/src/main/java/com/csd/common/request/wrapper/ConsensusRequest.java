package com.csd.common.request.wrapper;

import com.csd.common.request.IRequest;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;

import static com.csd.common.util.Serialization.*;

public class ConsensusRequest implements IRequest {

    private IRequest.Type type;
    private byte[] encodedRequest;
    private long lastEntryId;

    public ConsensusRequest(IRequest request, long lastEntryId) {
        this.type = request.type();
        this.encodedRequest = dataToBytes(request);
        this.lastEntryId = lastEntryId;
    }

    ConsensusRequest() {
    }

    public <T extends Serializable> T extractRequest() {
        return bytesToData(encodedRequest);
    }

    public IRequest.Type getType() {
        return type;
    }

    public void setType(IRequest.Type type) {
        this.type = type;
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
        ConsensusRequest that = (ConsensusRequest) o;
        return lastEntryId == that.lastEntryId && type == that.type && Arrays.equals(encodedRequest, that.encodedRequest);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(type, lastEntryId);
        result = 31 * result + Arrays.hashCode(encodedRequest);
        return result;
    }

    @Override
    public String toString() {
        return "ConsensualRequest{" +
                "type=" + type +
                ", encodedRequest=" + Arrays.toString(encodedRequest) +
                ", lastEntryId=" + lastEntryId +
                '}';
    }

    @Override
    public Type type() {
        return type;
    }
}
