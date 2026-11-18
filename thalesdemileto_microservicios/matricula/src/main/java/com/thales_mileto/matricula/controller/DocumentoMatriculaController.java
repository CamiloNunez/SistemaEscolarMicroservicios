package com.thales_mileto.matricula.controller;

import com.thales_mileto.matricula.model.DocumentoMatricula;
import com.thales_mileto.matricula.service.DocumentoMatriculaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/documentos-matricula")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DocumentoMatriculaController {

    private final DocumentoMatriculaService documentoMatriculaService;

    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(documentoMatriculaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(documentoMatriculaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody DocumentoMatricula documento, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentoMatriculaService.crear(documento, principal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody DocumentoMatricula documento) {
        return ResponseEntity.ok(documentoMatriculaService.actualizar(id, documento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        documentoMatriculaService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Documento eliminado correctamente"));
    }
}