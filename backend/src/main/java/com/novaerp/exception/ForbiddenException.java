package com.novaerp.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an authenticated user lacks the required role or authority.
 */
public class ForbiddenException extends BaseException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN_ACCESS");
    }
}
