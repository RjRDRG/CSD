package com.csd.common.request;

public class GetGlobalValueRequestBody extends Request {

    public GetGlobalValueRequestBody() {
    }

    @Override
    public byte[] serializedRequest() {
        return new byte[0];
    }
    @Override
    public String toString() {
        return "GetGlobalValueRequestBody{}";
    }
}
