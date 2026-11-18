package com.thales_mileto.academico.controller;

import com.thales_mileto.academico.model.Docente;
import com.thales_mileto.academico.service.DocenteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/docentes")
@CrossOrigin(origins = "*")
public class DocenteController {

    private final DocenteService docenteService;

    public DocenteController(DocenteService docenteService) {
        this.docenteService = docenteService;
    }

    @GetMapping
    public ResponseEntity<List<Docente>> listarTodos() {

        return ResponseEntity.ok(
                docenteService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Docente> buscarPorId(
            @PathVariable String id) {

        return docenteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Docente> buscarPorUsuarioId(
            @PathVariable String usuarioId) {

        return docenteService.buscarPorUsuarioId(usuarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/codigo/{codigoDocente}")
    public ResponseEntity<Docente> buscarPorCodigo(
            @PathVariable String codigoDocente) {

        return docenteService.buscarPorCodigo(codigoDocente)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Docente>> buscarPorEstado(
            @PathVariable String estado) {

        return ResponseEntity.ok(
                docenteService.buscarPorEstado(estado)
        );
    }

    @GetMapping("/especialidad/{especialidad}")
    public ResponseEntity<List<Docente>> buscarPorEspecialidad(
            @PathVariable String especialidad) {

        return ResponseEntity.ok(
                docenteService
                        .buscarPorEspecialidad(especialidad)
        );
    }

    /** Docentes cuyo usuarioId no coincide con un usuario PROFESOR vinculado en login. */
    @GetMapping("/vinculos/verificar")
    public ResponseEntity<List<Map<String, Object>>> verificarVinculos() {
        return ResponseEntity.ok(docenteService.verificarVinculos());
    }

    /** Registra en login el perfil de un docente existente cuyo usuario es válido. */
    @PostMapping("/{id}/vinculo/sincronizar")
    public ResponseEntity<Docente> sincronizarVinculo(@PathVariable String id) {
        return ResponseEntity.ok(docenteService.sincronizarVinculo(id));
    }

    @PostMapping
    public ResponseEntity<Docente> crear(
            @RequestBody Docente docente) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(docenteService.crear(docente));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Docente> actualizar(
            @PathVariable String id,
            @RequestBody Docente docente) {

        return ResponseEntity.ok(
                docenteService.actualizar(id, docente)
        );
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Docente> cambiarEstado(
            @PathVariable String id,
            @RequestParam String estado) {

        return ResponseEntity.ok(
                docenteService.cambiarEstado(id, estado)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String id) {

        docenteService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}