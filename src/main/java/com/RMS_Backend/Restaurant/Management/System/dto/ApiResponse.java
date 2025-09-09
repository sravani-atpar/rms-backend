package com.RMS_Backend.Restaurant.Management.System.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Integer errorCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();

    private List<ValidationError> validationErrors;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>()
                .setSuccess(true)
                .setMessage("Success")
                .setData(data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<T>()
                .setSuccess(true)
                .setMessage(message)
                .setData(data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<T>()
                .setSuccess(false)
                .setMessage(message);
    }

    public static <T> ApiResponse<T> error(String message, Integer errorCode) {
        return new ApiResponse<T>()
                .setSuccess(false)
                .setMessage(message)
                .setErrorCode(errorCode);
    }

    public ApiResponse<T> addValidationError(String field, String message) {
        if (validationErrors == null) {
            validationErrors = new ArrayList<>();
        }
        validationErrors.add(new ValidationError(field, message));
        return this;
    }

    @Data
    public static class ValidationError {
        private final String field;
        private final String message;
    }
}