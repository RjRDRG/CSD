package com.csd.common.request.wrapper;

import com.csd.common.request.Request;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import static com.csd.common.util.Serialization.*;

public class ConsensusRequest implements Serializable {

    public enum Type implements Serializable {
        LOAD, BALANCE, TRANSFER, EXTRACT, TOTAL_VAL, GLOBAL_VAL, LEDGER, BLOCK, DECRYPT, HIDDEN
    }

    private Type type;
    private byte[] encodedRequest;
    private long lastEntryId;

    public ConsensusRequest(Request request, Type type, long lastEntryId) {
        this.encodedRequest = dataToBytes(request);
        this.lastEntryId = lastEntryId;
    }

    public ConsensusRequest() {
    }

    public <T extends Serializable> T extractRequest() {
        return bytesToData(encodedRequest);
    }

    public long getLastEntryId() {
        return lastEntryId;
    }

    public void setLastEntryId(long lastEntryId) {
        this.lastEntryId = lastEntryId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public byte[] getEncodedRequest() {
        return encodedRequest;
    }

    public void setEncodedRequest(byte[] encodedRequest) {
        this.encodedRequest = encodedRequest;
    }
}
