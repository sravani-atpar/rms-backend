package com.RMS_Backend.Restaurant.Management.System.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends BaseException {
    private static final int ERROR_CODE = 400001;

    public ValidationException(String message) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }
}