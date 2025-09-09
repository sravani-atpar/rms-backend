package com.RMS_Backend.Restaurant.Management.System.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {
    private static final int ERROR_CODE = 404001;

    public ResourceNotFoundException(String message) {
        super(message, ERROR_CODE, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue),
                ERROR_CODE, HttpStatus.NOT_FOUND);
    }
}