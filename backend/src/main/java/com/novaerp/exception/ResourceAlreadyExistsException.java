package com.novaerp.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when attempting to create an entity that conflicts with existing unique constraints.
 */
public class ResourceAlreadyExistsException extends BaseException {

    public ResourceAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue),
                HttpStatus.CONFLICT,
                "RESOURCE_ALREADY_EXISTS");
    }

    public ResourceAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT, "RESOURCE_ALREADY_EXISTS");
    }
}
