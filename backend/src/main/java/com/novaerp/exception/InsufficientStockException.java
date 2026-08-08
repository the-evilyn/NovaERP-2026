package com.novaerp.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an inventory deduction exceeds available stock quantity.
 */
public class InsufficientStockException extends BaseException {

    public InsufficientStockException(String productCode, Integer requested, Integer available) {
        super(String.format("Insufficient stock for product [%s]. Requested: %d, Available: %d",
                productCode, requested, available),
                HttpStatus.UNPROCESSABLE_ENTITY,
                "INSUFFICIENT_STOCK");
    }

    public InsufficientStockException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, "INSUFFICIENT_STOCK");
    }
}
