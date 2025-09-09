package com.RMS_Backend.Restaurant.Management.System.exception;


import com.RMS_Backend.Restaurant.Management.System.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.ws.rs.InternalServerErrorException;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handle base exceptions (our custom exception hierarchy)
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException ex, HttpServletRequest request) {
        log.error("BaseException: {}", ex.getMessage(), ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        // Determine appropriate HTTP status from exception type
        if (ex instanceof ResourceNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (ex instanceof ValidationException ) {
            status = HttpStatus.BAD_REQUEST;
        }
        else if( ex instanceof DuplicateResourceException){
            status = HttpStatus.CONFLICT;
        }else if (ex instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
        }

        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), ex.getErrorCode());
        return new ResponseEntity<>(response, status);
    }

    // Handle validation exceptions from @Valid annotations
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        log.error("Validation error: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error("Validation failed", 400000);

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            response.addValidationError(error.getField(),
                    error.getDefaultMessage() != null ? error.getDefaultMessage() : "Validation error");
        }

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Handle constraint violation exceptions (for @Valid on method parameters)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {

        log.error("Constraint violation: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error("Validation failed", 400000);

        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String fieldName = violation.getPropertyPath().toString();
            response.addValidationError(fieldName, violation.getMessage());
        }

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Handle database integrity violations
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.error("Data integrity violation: {}", ex.getMessage(), ex);

        String message = "Database constraint violation";
        // Extract more useful message if possible
        if (ex.getMessage() != null && ex.getMessage().contains("duplicate key")) {
            message = "A record with the same unique identifier already exists";
        }

        ApiResponse<Void> response = ApiResponse.error(message, 400003);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {

        log.error("RuntimeException: {}", ex.getMessage(), ex);

        ApiResponse<Void> response = ApiResponse.error(
                "An unexpected error occurred: " + ex.getMessage(), 500000);

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(jakarta.servlet.ServletException.class)
    public ResponseEntity<ApiResponse<Void>> handleServletException(
            jakarta.servlet.ServletException ex, HttpServletRequest request) {

        log.error("ServletException: {}", ex.getMessage(), ex);

        ApiResponse<Void> response = ApiResponse.error(
                "A servlet error occurred: " + ex.getMessage(), 500001);

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ApiResponse<Void>> handleInternalServerErrorException(
            InternalServerErrorException ex, HttpServletRequest request) {

        log.error("InternalServerErrorException: {}", ex.getMessage(), ex);

        ApiResponse<Void> response = ApiResponse.error(
                "An internal server error occurred: " + ex.getMessage(), 500002);

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    // Handle NoSuchElementException (commonly thrown by Optional.get())
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoSuchElementException(
            NoSuchElementException ex, HttpServletRequest request) {

        log.error("No such element: {}", ex.getMessage(), ex);

        ApiResponse<Void> response = ApiResponse.error("Resource not found", 404000);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

     @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.error("AccessDeniedException: {}", ex.getMessage(), ex);

        ApiResponse<Void> response = ApiResponse.error("Access denied", 403000);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // Catch-all for any unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception: {}", ex.getMessage(), ex);

        ApiResponse<Void> response = ApiResponse.error(
                "An unexpected error occurred. Please try again later.", 500000);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
