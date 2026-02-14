package com.medicalcrm.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 Not found
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNoTFound(NotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(ex.getMessage());
    }

    // 400 Bad request
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusiness(BusinessException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    // Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex){
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
              .getFieldErrors()
              .forEach(err ->
                      errors.put(err.getField(), err.getDefaultMessage())
              );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // 500 Internal server error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleOther(Exception ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error");
    }

}
