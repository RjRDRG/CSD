package com.csd.common.request;

public class GetLedgerRequestBody implements IRequest {
    public GetLedgerRequestBody() {
    }

    @Override
    public Type type() {
        return Type.LEDGER;
    }
}
