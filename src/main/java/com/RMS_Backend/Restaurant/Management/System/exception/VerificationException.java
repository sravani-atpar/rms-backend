package com.RMS_Backend.Restaurant.Management.System.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class VerificationException extends RuntimeException {
    public VerificationException(String message) {
        super(message);
    }
}