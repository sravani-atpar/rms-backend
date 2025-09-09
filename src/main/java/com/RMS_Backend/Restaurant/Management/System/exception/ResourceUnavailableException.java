package com.RMS_Backend.Restaurant.Management.System.exception;

import org.springframework.http.HttpStatus;

public class ResourceUnavailableException extends BaseException {
    private static final int ERROR_CODE = 409001;

    public ResourceUnavailableException(String message) {
        super(message, ERROR_CODE, HttpStatus.CONFLICT);
    }

    public ResourceUnavailableException(String resourceType, String reason) {
        super(String.format("%s is unavailable: %s", resourceType, reason), ERROR_CODE, HttpStatus.CONFLICT);
    }
}