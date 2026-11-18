package com.thales_mileto.notificaciones.controller;

import com.thales_mileto.notificaciones.model.CanalNotificacion;
import com.thales_mileto.notificaciones.model.PlantillaNotificacion;
import com.thales_mileto.notificaciones.service.PlantillaNotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plantillas-notificacion")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlantillaNotificacionController {

    private final PlantillaNotificacionService plantillaService;

    @GetMapping
    public ResponseEntity<List<PlantillaNotificacion>> listarTodas() {
        return ResponseEntity.ok(
                plantillaService.listarTodas()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantillaNotificacion> buscarPorId(
            @PathVariable String id
    ) {
        return plantillaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<PlantillaNotificacion> buscarPorCodigo(
            @PathVariable String codigo
    ) {
        return plantillaService.buscarPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/evento/{evento}")
    public ResponseEntity<List<PlantillaNotificacion>> buscarPorEvento(
            @PathVariable String evento
    ) {
        return ResponseEntity.ok(
                plantillaService.buscarPorEvento(evento)
        );
    }

    @GetMapping("/canal/{canal}")
    public ResponseEntity<List<PlantillaNotificacion>> buscarPorCanal(
            @PathVariable CanalNotificacion canal
    ) {
        return ResponseEntity.ok(
                plantillaService.buscarPorCanal(canal)
        );
    }

    @GetMapping("/activas")
    public ResponseEntity<List<PlantillaNotificacion>> buscarActivas() {
        return ResponseEntity.ok(
                plantillaService.buscarActivas()
        );
    }

    @GetMapping("/inactivas")
    public ResponseEntity<List<PlantillaNotificacion>> buscarInactivas() {
        return ResponseEntity.ok(
                plantillaService.buscarInactivas()
        );
    }

    @GetMapping("/evento/{evento}/activas")
    public ResponseEntity<List<PlantillaNotificacion>> buscarActivasPorEvento(
            @PathVariable String evento
    ) {
        return ResponseEntity.ok(
                plantillaService.buscarActivasPorEvento(evento)
        );
    }

    @GetMapping("/canal/{canal}/activas")
    public ResponseEntity<List<PlantillaNotificacion>> buscarActivasPorCanal(
            @PathVariable CanalNotificacion canal
    ) {
        return ResponseEntity.ok(
                plantillaService.buscarActivasPorCanal(canal)
        );
    }

    @GetMapping("/evento/{evento}/canal/{canal}/activa")
    public ResponseEntity<PlantillaNotificacion> buscarPlantillaActiva(
            @PathVariable String evento,
            @PathVariable CanalNotificacion canal
    ) {
        return plantillaService.buscarPlantillaActiva(
                        evento,
                        canal
                )
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> guardar(
            @RequestBody PlantillaNotificacion plantilla
    ) {
        try {
            return ResponseEntity.ok(
                    plantillaService.guardar(plantilla)
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
            @RequestBody PlantillaNotificacion plantilla
    ) {
        try {
            return ResponseEntity.ok(
                    plantillaService.actualizar(
                            id,
                            plantilla
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<?> activar(
            @PathVariable String id
    ) {
        try {
            return ResponseEntity.ok(
                    plantillaService.activar(id)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivar(
            @PathVariable String id
    ) {
        try {
            return ResponseEntity.ok(
                    plantillaService.desactivar(id)
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
            plantillaService.eliminar(id);

            return ResponseEntity.ok(
                    "Plantilla eliminada correctamente."
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }
}