package com.RMS_Backend.Restaurant.Management.System.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException {
    private static final int ERROR_CODE = 401001;

    public UnauthorizedException(String message) {
        super(message, ERROR_CODE, HttpStatus.UNAUTHORIZED);
    }
}