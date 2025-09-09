package com.RMS_Backend.Restaurant.Management.System.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends BaseException {
    private static final int ERROR_CODE = 400002;

    public DuplicateResourceException(String message) {
        super(message, ERROR_CODE, HttpStatus.CONFLICT);
    }

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue),
                ERROR_CODE, HttpStatus.CONFLICT);
    }
}