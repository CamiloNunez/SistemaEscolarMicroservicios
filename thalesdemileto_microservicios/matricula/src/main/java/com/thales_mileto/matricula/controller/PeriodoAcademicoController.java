package com.thales_mileto.matricula.controller;

import com.thales_mileto.matricula.model.PeriodoAcademico;
import com.thales_mileto.matricula.service.PeriodoAcademicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/periodos-academicos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PeriodoAcademicoController {

    private final PeriodoAcademicoService periodoAcademicoService;

    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(periodoAcademicoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(periodoAcademicoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody PeriodoAcademico periodo, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(periodoAcademicoService.crear(periodo, principal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody PeriodoAcademico periodo) {
        return ResponseEntity.ok(periodoAcademicoService.actualizar(id, periodo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        periodoAcademicoService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Periodo académico eliminado correctamente"));
    }
}