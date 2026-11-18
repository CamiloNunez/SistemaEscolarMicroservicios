package com.thales_mileto.reportes.controller;

import com.thales_mileto.reportes.model.EventoAuditoria;
import com.thales_mileto.reportes.service.EventoAuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/eventos-auditoria")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EventoAuditoriaController {

    private final EventoAuditoriaService service;

    // =========================================================
    // LISTAR
    // =========================================================

    @GetMapping
    public ResponseEntity<List<EventoAuditoria>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoAuditoria> buscarPorId(
            @PathVariable String id
    ) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================
    // BÚSQUEDAS
    // =========================================================

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<EventoAuditoria>> buscarPorUsuario(
            @PathVariable String usuarioId
    ) {
        return ResponseEntity.ok(
                service.buscarPorUsuario(usuarioId)
        );
    }

    @GetMapping("/microservicio/{microservicio}")
    public ResponseEntity<List<EventoAuditoria>> buscarPorMicroservicio(
            @PathVariable String microservicio
    ) {
        return ResponseEntity.ok(
                service.buscarPorMicroservicio(microservicio)
        );
    }

    @GetMapping("/tipo-evento/{tipoEvento}")
    public ResponseEntity<List<EventoAuditoria>> buscarPorTipoEvento(
            @PathVariable String tipoEvento
    ) {
        return ResponseEntity.ok(
                service.buscarPorTipoEvento(tipoEvento)
        );
    }

    @GetMapping("/entidad/{entidad}")
    public ResponseEntity<List<EventoAuditoria>> buscarPorEntidad(
            @PathVariable String entidad
    ) {
        return ResponseEntity.ok(
                service.buscarPorEntidad(entidad)
        );
    }

    @GetMapping("/entidad-id/{entidadId}")
    public ResponseEntity<List<EventoAuditoria>> buscarPorEntidadId(
            @PathVariable String entidadId
    ) {
        return ResponseEntity.ok(
                service.buscarPorEntidadId(entidadId)
        );
    }

    @GetMapping("/accion/{accion}")
    public ResponseEntity<List<EventoAuditoria>> buscarPorAccion(
            @PathVariable String accion
    ) {
        return ResponseEntity.ok(
                service.buscarPorAccion(accion)
        );
    }

    @GetMapping("/usuario/{usuarioId}/microservicio/{microservicio}")
    public ResponseEntity<List<EventoAuditoria>> buscarPorUsuarioYMicroservicio(
            @PathVariable String usuarioId,
            @PathVariable String microservicio
    ) {
        return ResponseEntity.ok(
                service.buscarPorUsuarioYMicroservicio(
                        usuarioId,
                        microservicio
                )
        );
    }

    @GetMapping("/entidad/{entidad}/id/{entidadId}")
    public ResponseEntity<List<EventoAuditoria>> buscarPorEntidadYEntidadId(
            @PathVariable String entidad,
            @PathVariable String entidadId
    ) {
        return ResponseEntity.ok(
                service.buscarPorEntidadYEntidadId(
                        entidad,
                        entidadId
                )
        );
    }

    @GetMapping("/microservicio/{microservicio}/tipo-evento/{tipoEvento}")
    public ResponseEntity<List<EventoAuditoria>> buscarPorMicroservicioYTipoEvento(
            @PathVariable String microservicio,
            @PathVariable String tipoEvento
    ) {
        return ResponseEntity.ok(
                service.buscarPorMicroservicioYTipoEvento(
                        microservicio,
                        tipoEvento
                )
        );
    }

    // =========================================================
    // FECHAS
    // =========================================================

    @GetMapping("/fechas")
    public ResponseEntity<?> buscarPorFechas(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime inicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fin
    ) {
        try {
            return ResponseEntity.ok(
                    service.buscarPorFechas(inicio, fin)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    @GetMapping("/microservicio/{microservicio}/fechas")
    public ResponseEntity<?> buscarPorMicroservicioYFechas(
            @PathVariable String microservicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime inicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fin
    ) {
        try {
            return ResponseEntity.ok(
                    service.buscarPorMicroservicioYFechas(
                            microservicio,
                            inicio,
                            fin
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    @GetMapping("/usuario/{usuarioId}/fechas")
    public ResponseEntity<?> buscarPorUsuarioYFechas(
            @PathVariable String usuarioId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime inicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fin
    ) {
        try {
            return ResponseEntity.ok(
                    service.buscarPorUsuarioYFechas(
                            usuarioId,
                            inicio,
                            fin
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // =========================================================
    // CORRELATION ID
    // =========================================================

    @GetMapping("/correlation/{correlationId}")
    public ResponseEntity<List<EventoAuditoria>> buscarPorCorrelationId(
            @PathVariable String correlationId
    ) {
        return ResponseEntity.ok(
                service.buscarPorCorrelationId(correlationId)
        );
    }

    @GetMapping("/correlation/{correlationId}/ultimo")
    public ResponseEntity<EventoAuditoria> buscarUltimoPorCorrelationId(
            @PathVariable String correlationId
    ) {
        return service.buscarUltimoPorCorrelationId(correlationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================
    // CREAR
    // =========================================================

    @PostMapping
    public ResponseEntity<?> guardar(
            @RequestBody EventoAuditoria evento
    ) {
        try {
            return ResponseEntity.ok(
                    service.guardar(evento)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarEvento(
            @RequestBody RegistrarEventoRequest request
    ) {
        try {

            EventoAuditoria evento =
                    service.registrarEvento(
                            request.getUsuarioId(),
                            request.getMicroservicio(),
                            request.getTipoEvento(),
                            request.getEntidad(),
                            request.getEntidadId(),
                            request.getAccion(),
                            request.getDatos(),
                            request.getIp(),
                            request.getCorrelationId()
                    );

            return ResponseEntity.ok(evento);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "mensaje",
                            e.getMessage()
                    ));
        }
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable String id,
            @RequestBody EventoAuditoria evento
    ) {
        try {
            return ResponseEntity.ok(
                    service.actualizar(id, evento)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // =========================================================
    // ELIMINAR
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable String id
    ) {
        try {

            service.eliminar(id);

            return ResponseEntity.ok(
                    Map.of(
                            "mensaje",
                            "Evento de auditoría eliminado correctamente"
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "mensaje",
                            e.getMessage()
                    ));
        }
    }

    // =========================================================
    // DTO
    // =========================================================

    @lombok.Data
    public static class RegistrarEventoRequest {

        private String usuarioId;
        private String microservicio;
        private String tipoEvento;
        private String entidad;
        private String entidadId;
        private String accion;
        private Map<String, Object> datos;
        private String ip;
        private String correlationId;
    }
}