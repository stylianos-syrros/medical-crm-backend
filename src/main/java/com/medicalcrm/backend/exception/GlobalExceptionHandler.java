package com.medicalcrm.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

    // 401 Unauthorized (bad credentials, user not found, etc.)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ex.getMessage() != null ? ex.getMessage() : "Authentication failed");
    }

    // 500 Internal server error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleOther(Exception ex){
        try {
            String line = "{\"sessionId\":\"fb0aac\",\"location\":\"GlobalExceptionHandler.java:handleOther\",\"message\":\"handleOther\",\"data\":{\"exceptionClass\":\"" + ex.getClass().getName() + "\",\"message\":\"" + (ex.getMessage() != null ? ex.getMessage().replace("\"", "'") : "null") + "\"},\"timestamp\":" + System.currentTimeMillis() + ",\"hypothesisId\":\"A\"}\n";
            Files.write(Paths.get("/Users/ntk/Downloads/backend/.cursor/debug-fb0aac.log"), line.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Throwable t) { /* ignore */ }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error");
    }

}