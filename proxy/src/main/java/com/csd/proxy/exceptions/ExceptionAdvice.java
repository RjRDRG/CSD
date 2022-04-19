package com.csd.proxy.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


@ControllerAdvice
class ExceptionAdvice {

    @ResponseBody
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String notFoundHandler(NotFoundException ex) {
        return "NOT_FOUND|"+ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    String forbiddenHandler(ForbiddenException ex) {
        return "FORBIDDEN|"+ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(NotImplementedException.class)
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    String notImplementedHandler(NotImplementedException ex) {
        return "NOT_IMPLEMENTED|"+ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String badRequestHandler(BadRequestException ex) {
        return "BAD_REQUEST|"+ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(ServerErrorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String internalErrorHandler(ServerErrorException ex) {
        return "INTERNAL_SERVER_ERROR|"+ex.getMessage();
    }
}