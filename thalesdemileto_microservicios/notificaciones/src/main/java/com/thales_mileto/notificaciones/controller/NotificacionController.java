package com.thales_mileto.notificaciones.controller;

import com.thales_mileto.notificaciones.model.CanalNotificacion;
import com.thales_mileto.notificaciones.model.Notificacion;
import com.thales_mileto.notificaciones.service.NotificacionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    public ResponseEntity<List<Notificacion>> listarTodas() {
        return ResponseEntity.ok(
                notificacionService.listarTodas()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notificacion> buscarPorId(
            @PathVariable String id
    ) {
        return notificacionService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/plantilla/{plantillaId}")
    public ResponseEntity<List<Notificacion>> buscarPorPlantilla(
            @PathVariable String plantillaId
    ) {
        return ResponseEntity.ok(
                notificacionService.buscarPorPlantilla(
                        plantillaId
                )
        );
    }

    @GetMapping("/evento/{tipoEvento}")
    public ResponseEntity<List<Notificacion>> buscarPorEvento(
            @PathVariable String tipoEvento
    ) {
        return ResponseEntity.ok(
                notificacionService.buscarPorEvento(
                        tipoEvento
                )
        );
    }

    @GetMapping("/referencia/{referenciaId}")
    public ResponseEntity<List<Notificacion>> buscarPorReferencia(
            @PathVariable String referenciaId
    ) {
        return ResponseEntity.ok(
                notificacionService.buscarPorReferencia(
                        referenciaId
                )
        );
    }

    @GetMapping("/canal/{canal}")
    public ResponseEntity<List<Notificacion>> buscarPorCanal(
            @PathVariable CanalNotificacion canal
    ) {
        return ResponseEntity.ok(
                notificacionService.buscarPorCanal(
                        canal
                )
        );
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Notificacion>> buscarPorEstado(
            @PathVariable String estado
    ) {
        return ResponseEntity.ok(
                notificacionService.buscarPorEstado(
                        estado
                )
        );
    }

    @GetMapping("/evento/{tipoEvento}/estado/{estado}")
    public ResponseEntity<List<Notificacion>> buscarEventoYEstado(
            @PathVariable String tipoEvento,
            @PathVariable String estado
    ) {
        return ResponseEntity.ok(
                notificacionService.buscarPorEventoYEstado(
                        tipoEvento,
                        estado
                )
        );
    }

    @GetMapping("/canal/{canal}/estado/{estado}")
    public ResponseEntity<List<Notificacion>> buscarCanalYEstado(
            @PathVariable CanalNotificacion canal,
            @PathVariable String estado
    ) {
        return ResponseEntity.ok(
                notificacionService.buscarPorCanalYEstado(
                        canal,
                        estado
                )
        );
    }

    @GetMapping("/referencia/{referenciaId}/evento/{tipoEvento}")
    public ResponseEntity<List<Notificacion>> buscarReferenciaYEvento(
            @PathVariable String referenciaId,
            @PathVariable String tipoEvento
    ) {
        return ResponseEntity.ok(
                notificacionService.buscarPorReferenciaYEvento(
                        referenciaId,
                        tipoEvento
                )
        );
    }

    @GetMapping("/fechas")
    public ResponseEntity<List<Notificacion>> buscarPorFechas(

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime inicio,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime fin
    ) {
        return ResponseEntity.ok(
                notificacionService.buscarPorFechas(
                        inicio,
                        fin
                )
        );
    }

    @GetMapping("/estado/{estado}/fechas")
    public ResponseEntity<List<Notificacion>> buscarEstadoYFechas(

            @PathVariable String estado,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime inicio,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime fin
    ) {
        return ResponseEntity.ok(
                notificacionService.buscarPorEstadoYFechas(
                        estado,
                        inicio,
                        fin
                )
        );
    }

    @PostMapping
    public ResponseEntity<?> guardar(
            @RequestBody Notificacion notificacion
    ) {
        try {
            return ResponseEntity.ok(
                    notificacionService.guardar(
                            notificacion
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PostMapping("/crear-desde-plantilla")
    public ResponseEntity<?> crearDesdePlantilla(
            @RequestBody CrearDesdePlantillaRequest request
    ) {
        try {
            return ResponseEntity.ok(
                    notificacionService.crearDesdePlantilla(
                            request.getCodigoPlantilla(),
                            request.getReferenciaId(),
                            request.getVariables()
                    )
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
            @RequestBody Notificacion notificacion
    ) {
        try {
            return ResponseEntity.ok(
                    notificacionService.actualizar(
                            id,
                            notificacion
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PatchMapping("/{id}/enviada")
    public ResponseEntity<?> marcarEnviada(
            @PathVariable String id
    ) {
        try {
            return ResponseEntity.ok(
                    notificacionService.marcarEnviada(id)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PatchMapping("/{id}/fallida")
    public ResponseEntity<?> marcarFallida(
            @PathVariable String id
    ) {
        try {
            return ResponseEntity.ok(
                    notificacionService.marcarFallida(id)
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
            notificacionService.eliminar(id);

            return ResponseEntity.ok(
                    "Notificación eliminada correctamente."
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @Data
    public static class CrearDesdePlantillaRequest {

        private String codigoPlantilla;

        private String referenciaId;

        private Map<String, String> variables;
    }
}