package com.dropp.foodcatalog.config;

import com.dropp.foodcatalog.exception.RestaurantNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RestaurantNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleRestaurantNotFound(RestaurantNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, String>> handleRestaurantServiceUnavailable(ResourceAccessException ex) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Restaurant service is currently unavailable. Please try again later."));
    }
}