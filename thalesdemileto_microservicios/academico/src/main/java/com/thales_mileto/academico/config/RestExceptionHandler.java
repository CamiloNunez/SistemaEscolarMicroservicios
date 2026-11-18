package com.thales_mileto.academico.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    /** Datos inválidos enviados por el cliente. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> datosInvalidos(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensaje", e.getMessage()));
    }

    /** Conflicto con el estado de otro servicio (vinculación con login). */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflicto(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
    }
}
