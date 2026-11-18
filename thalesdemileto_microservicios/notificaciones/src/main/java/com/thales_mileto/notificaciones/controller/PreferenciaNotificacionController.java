package com.thales_mileto.notificaciones.controller;

import com.thales_mileto.notificaciones.model.PreferenciaNotificacion;
import com.thales_mileto.notificaciones.service.PreferenciaNotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/preferencias-notificacion")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PreferenciaNotificacionController {

    private final PreferenciaNotificacionService preferenciaService;

    @GetMapping
    public ResponseEntity<List<PreferenciaNotificacion>> listarTodas() {
        return ResponseEntity.ok(
                preferenciaService.listarTodas()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PreferenciaNotificacion> buscarPorId(
            @PathVariable String id
    ) {
        return preferenciaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<PreferenciaNotificacion> buscarPorUsuario(
            @PathVariable String usuarioId
    ) {
        return preferenciaService.buscarPorUsuario(usuarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}/obtener-o-crear")
    public ResponseEntity<?> obtenerOCrear(
            @PathVariable String usuarioId
    ) {
        try {
            return ResponseEntity.ok(
                    preferenciaService.obtenerOCrear(
                            usuarioId
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PostMapping
    public ResponseEntity<?> guardar(
            @RequestBody PreferenciaNotificacion preferencia
    ) {
        try {
            return ResponseEntity.ok(
                    preferenciaService.guardar(preferencia)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PutMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> actualizar(
            @PathVariable String usuarioId,
            @RequestBody PreferenciaNotificacion preferencia
    ) {
        try {
            return ResponseEntity.ok(
                    preferenciaService.actualizar(
                            usuarioId,
                            preferencia
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @DeleteMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> eliminarPorUsuario(
            @PathVariable String usuarioId
    ) {
        try {
            preferenciaService.eliminarPorUsuario(
                    usuarioId
            );

            return ResponseEntity.ok(
                    "Preferencias eliminadas correctamente."
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }
}