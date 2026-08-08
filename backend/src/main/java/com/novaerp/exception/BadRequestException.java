package com.novaerp.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when client provides malformed or semantically invalid parameters.
 */
public class BadRequestException extends BaseException {

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }
}
