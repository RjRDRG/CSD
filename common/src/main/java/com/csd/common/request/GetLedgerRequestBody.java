package com.csd.common.request;

import java.util.UUID;

import static com.csd.common.util.Serialization.dataToBytesDeterministic;

public class GetLedgerRequestBody extends Request {
    public GetLedgerRequestBody() {
        this.requestId = UUID.randomUUID().toString();
    }

    @Override
    public byte[] serializedRequest() {
        return dataToBytesDeterministic(requestId);
    }

    @Override
    public String toString() {
        return "GetLedgerRequestBody{}";
    }
}
