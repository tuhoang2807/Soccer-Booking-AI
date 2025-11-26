package com.example.soccer_booking_server.exception;

import com.example.soccer_booking_server.dto.ResponseFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseFormat<Object>> handleNotFound(NotFoundException ex) {
        ResponseFormat<Object> response = new ResponseFormat<>(404, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseFormat<Object>> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        ResponseFormat<Object> response = new ResponseFormat<>(403, "Không có quyền truy cập", null);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseFormat<Object>> handleGlobalException(Exception ex) {
        ResponseFormat<Object> response = new ResponseFormat<>(500, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ValidException.class)
    public ResponseEntity<ResponseFormat<Object>> handleValid(ValidException ex) {
        ResponseFormat<Object> response = new ResponseFormat<>(400, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}

