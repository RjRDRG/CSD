package com.csd.common.request;

public class GetGlobalValueRequestBody implements IRequest {

    public GetGlobalValueRequestBody() {
    }

    @Override
    public String toString() {
        return "GetGlobalValueRequestBody{}";
    }

    @Override
    public Type type() {
        return Type.GLOBAL_VAL;
    }
}
