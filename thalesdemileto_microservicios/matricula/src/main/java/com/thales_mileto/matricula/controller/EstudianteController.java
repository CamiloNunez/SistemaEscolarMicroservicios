package com.thales_mileto.matricula.controller;

import com.thales_mileto.matricula.model.Estudiante;
import com.thales_mileto.matricula.service.EstudianteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/estudiantes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EstudianteController {

    private final EstudianteService estudianteService;

    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(estudianteService.listar());
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(estudianteService.buscarPorNombre(nombre));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(estudianteService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Estudiante estudiante, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(estudianteService.crear(estudiante, principal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Estudiante estudiante) {
        return ResponseEntity.ok(estudianteService.actualizar(id, estudiante));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        estudianteService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Estudiante eliminado correctamente"));
    }
}