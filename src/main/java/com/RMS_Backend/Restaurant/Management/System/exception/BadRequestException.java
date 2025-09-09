package com.RMS_Backend.Restaurant.Management.System.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {
    private static final int ERROR_CODE = 400000;

    public BadRequestException(String message) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }
}