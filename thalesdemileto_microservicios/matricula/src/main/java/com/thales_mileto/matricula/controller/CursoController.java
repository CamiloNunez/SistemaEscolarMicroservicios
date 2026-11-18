package com.thales_mileto.matricula.controller;

import com.thales_mileto.matricula.model.Curso;
import com.thales_mileto.matricula.service.CursoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/cursos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CursoController {

    private final CursoService cursoService;

    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(cursoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(cursoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Curso curso, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cursoService.crear(curso, principal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Curso curso) {
        return ResponseEntity.ok(cursoService.actualizar(id, curso));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        cursoService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Curso eliminado correctamente"));
    }
}