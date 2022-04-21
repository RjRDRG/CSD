package com.csd.common.request;

public class GetGlobalValueRequestBody implements IRequest {
    public GetGlobalValueRequestBody() {
    }

    @Override
    public Type type() {
        return Type.GLOBAL_VAL;
    }
}
