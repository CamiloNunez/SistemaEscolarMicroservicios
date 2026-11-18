package com.thales_mileto.matricula.controller;

import com.thales_mileto.matricula.model.Matricula;
import com.thales_mileto.matricula.service.MatriculaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matriculas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MatriculaController {

    private final MatriculaService matriculaService;

    @GetMapping("/public/estado")
    public ResponseEntity<?> estadoPublico() {
        return ResponseEntity.ok(Map.of(
                "servicio", "matricula",
                "estado", "activo"
        ));
    }

    @GetMapping
    public ResponseEntity<?> listar(Principal principal) {
        return ResponseEntity.ok(Map.of(
                "mensaje", "Token validado correctamente",
                "usuario", principal.getName(),
                "matriculas", matriculaService.listar()
        ));
    }

    /**
     * Importa matrículas desde otro sistema. Cada elemento debe traer
     * referenciaMigracion { sistemaOrigen, codigoExterno, ... }. Reenviar el lote no duplica.
     */
    @PostMapping("/migracion")
    public ResponseEntity<?> migrar(
            @RequestBody List<Matricula> matriculas,
            @RequestParam(required = false) String lote,
            Principal principal
    ) {
        return ResponseEntity.ok(matriculaService.migrar(matriculas, lote, principal));
    }

    /** Busca la matrícula creada a partir de un registro del sistema de origen. */
    @GetMapping("/migracion/referencia")
    public ResponseEntity<?> buscarPorReferencia(
            @RequestParam String sistemaOrigen,
            @RequestParam String codigoExterno
    ) {
        return matriculaService.buscarPorReferencia(sistemaOrigen.toUpperCase(), codigoExterno)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "No hay matrícula migrada con esa referencia")));
    }

    @GetMapping("/migracion/lote/{lote}")
    public ResponseEntity<?> listarPorLote(@PathVariable String lote) {
        return ResponseEntity.ok(matriculaService.listarPorLote(lote));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(matriculaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestBody Matricula matricula,
            Principal principal
    ) {
        Matricula nueva = matriculaService.crear(matricula, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable String id,
            @RequestBody Matricula matricula
    ) {
        return ResponseEntity.ok(matriculaService.actualizar(id, matricula));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        matriculaService.eliminar(id);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Matrícula eliminada correctamente"
        ));
    }
}