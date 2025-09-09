package com.RMS_Backend.Restaurant.Management.System.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends BaseException {
    private static final int ERROR_CODE = 403000;

    public AccessDeniedException(String message) {
        super(message, ERROR_CODE, HttpStatus.FORBIDDEN);
    }
}