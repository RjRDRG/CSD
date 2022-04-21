package com.csd.common.request;

import java.io.Serializable;

public interface IRequest extends Serializable {
    enum Type implements Serializable {
        LOAD, BALANCE
    }

    Type type();
}
