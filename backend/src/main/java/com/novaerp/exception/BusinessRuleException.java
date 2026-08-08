package com.novaerp.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an ERP domain invariant or business rule is violated.
 */
public class BusinessRuleException extends BaseException {

    public BusinessRuleException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_RULE_VIOLATION");
    }

    public BusinessRuleException(String message, String errorCode) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, errorCode);
    }
}
