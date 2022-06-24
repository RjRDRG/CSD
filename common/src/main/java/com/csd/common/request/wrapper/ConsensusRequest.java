package com.csd.common.request.wrapper;

import com.csd.common.request.Request;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import static com.csd.common.util.Serialization.*;

public class ConsensusRequest implements Serializable {

    public enum Type implements Serializable {
        LOAD, BALANCE, TRANSFER, EXTRACT, TOTAL_VAL, GLOBAL_VAL, LEDGER
    }

    private Type type;
    private byte[] encodedRequest;

    public ConsensusRequest(Request request, Type type) {
        this.encodedRequest = dataToBytes(request);
        this.type = type;
    }

    public ConsensusRequest() {
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
