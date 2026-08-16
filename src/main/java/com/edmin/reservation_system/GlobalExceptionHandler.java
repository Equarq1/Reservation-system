package com.edmin.reservation_system;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception e) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto("Internal server error", e.getMessage(), LocalDateTime.now());
        log.error("Handle Exception", e);
        return ResponseEntity.status(500).body(errorResponseDto);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEntityNotFound(EntityNotFoundException e) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto("Entity not found", e.getMessage(), LocalDateTime.now());
        log.error("Entity not found", e);
        return ResponseEntity.status(404).body(errorResponseDto);
    }

    @ExceptionHandler(exception = {IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponseDto> handleBadRequest(Exception e) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto("Bad Request", e.getMessage(), LocalDateTime.now());
        log.error("Bad Request", e);
        return ResponseEntity.status(400).body(errorResponseDto);
    }



}
