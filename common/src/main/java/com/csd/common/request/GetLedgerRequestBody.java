package com.csd.common.request;

public class GetLedgerRequestBody extends Request {
    public GetLedgerRequestBody() {
    }

    @Override
    public byte[] serializedRequest() {
        return new byte[0];
    }

    @Override
    public String toString() {
        return "GetLedgerRequestBody{}";
    }
}
