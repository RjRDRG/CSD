package com.csd.proxy.exceptions;

import com.csd.common.traits.Result;

import java.io.Serializable;

public class ExceptionMapper {

    public static <T extends Serializable> void throwPossibleException(Result<T> result) throws RuntimeException {
        if (result.isOK()) return;

        switch (result.error()) {
            case NOT_FOUND:
                System.out.println("dasdsad");
                throw new NotFoundException(result.message());
            case FORBIDDEN:
                throw new ForbiddenException(result.message());
            case NOT_IMPLEMENTED:
                throw new NotImplementedException(result.message());
            case INTERNAL_ERROR:
                throw new ServerErrorException(result.message());
        }
    }
}
