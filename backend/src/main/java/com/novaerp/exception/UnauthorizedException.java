package com.novaerp.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an unauthenticated request attempts to access a protected resource.
 */
public class UnauthorizedException extends BaseException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }
}
