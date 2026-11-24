package com.example.javatest.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex,
                                                                HttpServletRequest request) {
        List<Map<String, String>> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
        return build(HttpStatus.BAD_REQUEST, "Solicitud invalida", detalles, request.getRequestURI());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex,
                                                                         HttpServletRequest request) {
        List<Map<String, String>> detalles = ex.getConstraintViolations().stream()
                .map(v -> Map.of("campo", String.valueOf(v.getPropertyPath()), "mensaje", v.getMessage()))
                .collect(Collectors.toList());
        return build(HttpStatus.BAD_REQUEST, "Solicitud invalida", detalles, request.getRequestURI());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex, HttpServletRequest request) {
        List<Map<String, String>> detalles = List.of(Map.of("mensaje", "Formato o parametro de solicitud invalido"));
        return build(HttpStatus.BAD_REQUEST, "Solicitud invalida", detalles, request.getRequestURI());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        List<Map<String, String>> detalles = List.of(Map.of("mensaje", ex.getMessage()));
        return build(HttpStatus.BAD_REQUEST, "Solicitud invalida", detalles, request.getRequestURI());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        List<Map<String, String>> detalles = List.of(Map.of("mensaje", ex.getMessage()));
        return build(HttpStatus.NOT_FOUND, "Recurso no encontrado", detalles, request.getRequestURI());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex, HttpServletRequest request) {
        List<Map<String, String>> detalles = List.of(Map.of("mensaje", ex.getMessage()));
        return build(HttpStatus.CONFLICT, "Conflicto", detalles, request.getRequestURI());
    }

    private Map<String, String> toDetail(FieldError error) {
        Map<String, String> detail = new LinkedHashMap<>();
        detail.put("campo", error.getField());
        detail.put("mensaje", error.getDefaultMessage());
        return detail;
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String error, List<Map<String, String>> detalles,
                                                      String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("detalles", detalles);
        body.put("path", path);
        return ResponseEntity.status(status).body(body);
    }
}
