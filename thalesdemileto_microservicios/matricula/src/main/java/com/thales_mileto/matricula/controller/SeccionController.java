package com.thales_mileto.matricula.controller;

import com.thales_mileto.matricula.model.Seccion;
import com.thales_mileto.matricula.service.SeccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/secciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SeccionController {

    private final SeccionService seccionService;

    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(seccionService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(seccionService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Seccion seccion, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(seccionService.crear(seccion, principal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Seccion seccion) {
        return ResponseEntity.ok(seccionService.actualizar(id, seccion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        seccionService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Sección eliminada correctamente"));
    }
}