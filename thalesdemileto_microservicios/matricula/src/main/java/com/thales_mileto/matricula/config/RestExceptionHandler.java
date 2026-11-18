package com.thales_mileto.matricula.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
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

    /**
     * PostgreSQL rechazó la escritura por una restricción (UNIQUE duplicado, o el CHECK
     * ck_vacante_cupos si dos operaciones intentaran sobrevender una sección).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> restriccion(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "mensaje", "La operación viola una restricción de integridad de la base de datos",
                "detalle", String.valueOf(e.getMostSpecificCause().getMessage())));
    }

    /** Otra transacción tiene bloqueada la misma fila (p. ej. la vacante) y no se pudo esperar. */
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> bloqueo(PessimisticLockingFailureException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "mensaje", "El registro está siendo modificado por otra operación. Intente nuevamente"));
    }
}
