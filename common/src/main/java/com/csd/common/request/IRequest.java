package com.csd.common.request;

import java.io.Serializable;

public interface IRequest extends Serializable {
    enum Type implements Serializable {
        LOAD, BALANCE, TRANSFER, EXTRACT, TOTAL_VAL, GLOBAL_VAL, LEDGER, SESSION, PROPOSE
    }

    Type type();

    class Void implements IRequest{
        @Override
        public Type type() {
            return null;
        }
    }

}
