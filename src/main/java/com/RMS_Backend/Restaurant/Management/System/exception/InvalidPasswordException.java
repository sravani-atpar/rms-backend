package com.RMS_Backend.Restaurant.Management.System.exception;

import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends BaseException {
    private static final int ERROR_CODE = 401002;

    public InvalidPasswordException(String message) {
        super(message, ERROR_CODE, HttpStatus.UNAUTHORIZED);
    }

    public InvalidPasswordException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE, HttpStatus.UNAUTHORIZED);
    }
}