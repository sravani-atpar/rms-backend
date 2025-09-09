package com.RMS_Backend.Restaurant.Management.System.exception;

import org.springframework.http.HttpStatus;

public class DataAccessException extends BaseException {
    private static final int ERROR_CODE = 500001;

    public DataAccessException(String message) {
        super(message, ERROR_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}