package com.csd.common.request;

public class GetExtractRequestBody implements IRequest {

    public GetExtractRequestBody() {
    }

    @Override
    public String toString() {
        return "GetExtractRequestBody{}";
    }

    @Override
    public Type type() {
        return Type.EXTRACT;
    }
}
