package com.thales_mileto.notificaciones.controller;

import com.thales_mileto.notificaciones.model.DestinatarioNotificacion;
import com.thales_mileto.notificaciones.service.DestinatarioNotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/destinatarios-notificacion")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DestinatarioNotificacionController {

    private final DestinatarioNotificacionService destinatarioService;

    @GetMapping
    public ResponseEntity<List<DestinatarioNotificacion>> listarTodos() {
        return ResponseEntity.ok(
                destinatarioService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DestinatarioNotificacion> buscarPorId(
            @PathVariable String id
    ) {
        return destinatarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/notificacion/{notificacionId}")
    public ResponseEntity<List<DestinatarioNotificacion>> buscarPorNotificacion(
            @PathVariable String notificacionId
    ) {
        return ResponseEntity.ok(
                destinatarioService.buscarPorNotificacion(
                        notificacionId
                )
        );
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<DestinatarioNotificacion>> buscarPorUsuario(
            @PathVariable String usuarioId
    ) {
        return ResponseEntity.ok(
                destinatarioService.buscarPorUsuario(
                        usuarioId
                )
        );
    }

    @GetMapping("/tipo/{tipoDestinatario}")
    public ResponseEntity<List<DestinatarioNotificacion>> buscarPorTipo(
            @PathVariable String tipoDestinatario
    ) {
        return ResponseEntity.ok(
                destinatarioService.buscarPorTipoDestinatario(
                        tipoDestinatario
                )
        );
    }

    @GetMapping("/estado/{estadoEntrega}")
    public ResponseEntity<List<DestinatarioNotificacion>> buscarPorEstado(
            @PathVariable String estadoEntrega
    ) {
        return ResponseEntity.ok(
                destinatarioService.buscarPorEstadoEntrega(
                        estadoEntrega
                )
        );
    }

    @GetMapping("/notificacion/{notificacionId}/estado/{estadoEntrega}")
    public ResponseEntity<List<DestinatarioNotificacion>> buscarNotificacionYEstado(
            @PathVariable String notificacionId,
            @PathVariable String estadoEntrega
    ) {
        return ResponseEntity.ok(
                destinatarioService.buscarPorNotificacionYEstado(
                        notificacionId,
                        estadoEntrega
                )
        );
    }

    @GetMapping("/usuario/{usuarioId}/estado/{estadoEntrega}")
    public ResponseEntity<List<DestinatarioNotificacion>> buscarUsuarioYEstado(
            @PathVariable String usuarioId,
            @PathVariable String estadoEntrega
    ) {
        return ResponseEntity.ok(
                destinatarioService.buscarPorUsuarioYEstado(
                        usuarioId,
                        estadoEntrega
                )
        );
    }

    @GetMapping("/tipo/{tipoDestinatario}/estado/{estadoEntrega}")
    public ResponseEntity<List<DestinatarioNotificacion>> buscarTipoYEstado(
            @PathVariable String tipoDestinatario,
            @PathVariable String estadoEntrega
    ) {
        return ResponseEntity.ok(
                destinatarioService.buscarPorTipoYEstado(
                        tipoDestinatario,
                        estadoEntrega
                )
        );
    }

    @PostMapping
    public ResponseEntity<?> guardar(
            @RequestBody DestinatarioNotificacion destinatario
    ) {
        try {
            return ResponseEntity.ok(
                    destinatarioService.guardar(destinatario)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable String id,
            @RequestBody DestinatarioNotificacion destinatario
    ) {
        try {
            return ResponseEntity.ok(
                    destinatarioService.actualizar(
                            id,
                            destinatario
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PatchMapping("/{id}/entregado")
    public ResponseEntity<?> marcarEntregado(
            @PathVariable String id
    ) {
        try {
            return ResponseEntity.ok(
                    destinatarioService.marcarEntregado(id)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PatchMapping("/{id}/fallido")
    public ResponseEntity<?> marcarFallido(
            @PathVariable String id
    ) {
        try {
            return ResponseEntity.ok(
                    destinatarioService.marcarFallido(id)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable String id
    ) {
        try {
            destinatarioService.eliminar(id);

            return ResponseEntity.ok(
                    "Destinatario eliminado correctamente."
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }
}