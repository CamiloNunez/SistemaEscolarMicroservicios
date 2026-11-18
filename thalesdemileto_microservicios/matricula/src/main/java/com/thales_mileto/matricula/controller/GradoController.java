package com.thales_mileto.matricula.controller;

import com.thales_mileto.matricula.model.Grado;
import com.thales_mileto.matricula.service.GradoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/grados")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GradoController {

    private final GradoService gradoService;

    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(gradoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(gradoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Grado grado, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gradoService.crear(grado, principal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Grado grado) {
        return ResponseEntity.ok(gradoService.actualizar(id, grado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        gradoService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Grado eliminado correctamente"));
    }
}