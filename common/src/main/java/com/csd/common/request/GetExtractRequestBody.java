package com.csd.common.request;

import java.util.Arrays;
import java.util.Objects;

import static com.csd.common.util.Serialization.bytesToString;

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
