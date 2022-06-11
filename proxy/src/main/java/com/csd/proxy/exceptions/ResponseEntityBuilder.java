package com.csd.proxy.exceptions;

import com.csd.common.response.wrapper.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;

public class ResponseEntityBuilder {

    public static <T extends Serializable> ResponseEntity<Response<T>> buildResponse(Response<T> response) {
        HttpStatus httpStatus;

        switch (response.error()) {
            case OK:
                httpStatus = HttpStatus.OK;
                break;
            case BAD_REQUEST:
                httpStatus = HttpStatus.BAD_REQUEST;
                break;
            case NOT_FOUND:
                httpStatus = HttpStatus.NOT_FOUND;
                break;
            case FORBIDDEN:
                httpStatus = HttpStatus.FORBIDDEN;
                break;
            case NOT_IMPLEMENTED:
                httpStatus = HttpStatus.NOT_IMPLEMENTED;
                break;
            case INTERNAL_ERROR:
            default:
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(response, httpStatus);
    }
}
