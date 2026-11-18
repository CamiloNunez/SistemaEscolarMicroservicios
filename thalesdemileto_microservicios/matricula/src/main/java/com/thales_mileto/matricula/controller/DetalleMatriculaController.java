package com.thales_mileto.matricula.controller;

import com.thales_mileto.matricula.model.DetalleMatricula;
import com.thales_mileto.matricula.service.DetalleMatriculaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/detalles-matricula")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DetalleMatriculaController {

    private final DetalleMatriculaService detalleMatriculaService;

    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(detalleMatriculaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(detalleMatriculaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody DetalleMatricula detalle, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(detalleMatriculaService.crear(detalle, principal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody DetalleMatricula detalle) {
        return ResponseEntity.ok(detalleMatriculaService.actualizar(id, detalle));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        detalleMatriculaService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Detalle eliminado correctamente"));
    }
}