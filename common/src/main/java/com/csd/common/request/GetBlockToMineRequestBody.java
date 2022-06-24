package com.csd.common.request;

public class GetBlockToMineRequestBody implements IRequest {
    @Override
    public Type type() {
        return Type.MINE;
    }
}
