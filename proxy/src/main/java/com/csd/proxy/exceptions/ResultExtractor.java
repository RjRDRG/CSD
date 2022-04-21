package com.csd.proxy.exceptions;

import com.csd.common.traits.Result;

import java.io.Serializable;

public class ResultExtractor {

    public static <T extends Serializable> T value(Result<T> result) throws RuntimeException {
        if (result.isOK()) return result.value();

        switch (result.error()) {
            case NOT_FOUND:
                throw new NotFoundException(result.message());
            case FORBIDDEN:
                throw new ForbiddenException(result.message());
            case NOT_IMPLEMENTED:
                throw new NotImplementedException(result.message());
            case INTERNAL_ERROR:
            default:
                throw new ServerErrorException(result.message());
        }
    }
}
