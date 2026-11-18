package com.thales_mileto.matricula.controller;

import com.thales_mileto.matricula.model.PagoMatricula;
import com.thales_mileto.matricula.service.PagoMatriculaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos-matricula")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PagoMatriculaController {

    private final PagoMatriculaService pagoMatriculaService;

    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(pagoMatriculaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(pagoMatriculaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody PagoMatricula pago, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pagoMatriculaService.crear(pago, principal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody PagoMatricula pago) {
        return ResponseEntity.ok(pagoMatriculaService.actualizar(id, pago));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        pagoMatriculaService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Pago eliminado correctamente"));
    }
}