package com.thales_mileto.matricula.controller;

import com.thales_mileto.matricula.model.ProgramaAcademico;
import com.thales_mileto.matricula.service.ProgramaAcademicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/programas-academicos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProgramaAcademicoController {

    private final ProgramaAcademicoService programaAcademicoService;

    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(programaAcademicoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(programaAcademicoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody ProgramaAcademico programa, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(programaAcademicoService.crear(programa, principal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody ProgramaAcademico programa) {
        return ResponseEntity.ok(programaAcademicoService.actualizar(id, programa));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        programaAcademicoService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Programa académico eliminado correctamente"));
    }
}