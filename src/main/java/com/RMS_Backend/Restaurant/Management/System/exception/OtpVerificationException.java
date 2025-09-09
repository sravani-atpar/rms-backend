package com.RMS_Backend.Restaurant.Management.System.exception;

import org.springframework.http.HttpStatus;

public class OtpVerificationException extends BaseException {
    private static final int ERROR_CODE = 400005;

    public OtpVerificationException(String message) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }
}