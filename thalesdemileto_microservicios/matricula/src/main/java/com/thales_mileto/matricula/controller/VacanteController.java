package com.thales_mileto.matricula.controller;

import com.thales_mileto.matricula.model.Vacante;
import com.thales_mileto.matricula.service.VacanteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/vacantes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VacanteController {

    private final VacanteService vacanteService;

    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(vacanteService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(vacanteService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Vacante vacante, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vacanteService.crear(vacante, principal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Vacante vacante) {
        return ResponseEntity.ok(vacanteService.actualizar(id, vacante));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        vacanteService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Vacante eliminada correctamente"));
    }
}