package com.thales_mileto.matricula.controller;

import com.thales_mileto.matricula.model.PlanEstudios;
import com.thales_mileto.matricula.service.PlanEstudiosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/planes-estudio")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlanEstudiosController {

    private final PlanEstudiosService planEstudiosService;

    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(planEstudiosService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(planEstudiosService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody PlanEstudios plan, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(planEstudiosService.crear(plan, principal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody PlanEstudios plan) {
        return ResponseEntity.ok(planEstudiosService.actualizar(id, plan));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        planEstudiosService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Plan de estudios eliminado correctamente"));
    }
}