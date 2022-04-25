package com.csd.common.request;

public class GetBalanceRequestBody implements IRequest {

    public GetBalanceRequestBody() {
    }

    @Override
    public String toString() {
        return "GetBalanceRequestBody{}";
    }

    @Override
    public Type type() {
        return Type.BALANCE;
    }
}
