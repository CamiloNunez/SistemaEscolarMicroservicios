package com.thales_mileto.academico.controller;

import com.thales_mileto.academico.model.Prerequisito;
import com.thales_mileto.academico.service.PrerequisitoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prerequisitos")
@CrossOrigin(origins = "*")
public class PrerequisitoController {

    private final PrerequisitoService prerequisitoService;

    public PrerequisitoController(
            PrerequisitoService prerequisitoService) {
        this.prerequisitoService = prerequisitoService;
    }

    @GetMapping
    public ResponseEntity<List<Prerequisito>> listarTodos() {

        return ResponseEntity.ok(
                prerequisitoService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prerequisito> buscarPorId(
            @PathVariable String id) {

        return ResponseEntity.ok(
                prerequisitoService.buscarPorId(id)
        );
    }

    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<List<Prerequisito>> buscarPorCurso(
            @PathVariable String cursoId) {

        return ResponseEntity.ok(
                prerequisitoService.buscarPorCurso(cursoId)
        );
    }

    @GetMapping("/curso/{cursoId}/activos")
    public ResponseEntity<List<Prerequisito>> buscarActivosPorCurso(
            @PathVariable String cursoId) {

        return ResponseEntity.ok(
                prerequisitoService
                        .buscarActivosPorCurso(cursoId)
        );
    }

    @GetMapping("/curso/{cursoId}/estado/{estado}")
    public ResponseEntity<List<Prerequisito>> buscarPorCursoYEstado(
            @PathVariable String cursoId,
            @PathVariable String estado) {

        return ResponseEntity.ok(
                prerequisitoService.buscarPorCursoYEstado(
                        cursoId,
                        estado
                )
        );
    }

    @GetMapping("/requerido/{cursoPrerequisitoId}")
    public ResponseEntity<List<Prerequisito>> buscarCursosQueRequieren(
            @PathVariable String cursoPrerequisitoId) {

        return ResponseEntity.ok(
                prerequisitoService.buscarCursosQueRequieren(
                        cursoPrerequisitoId
                )
        );
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Prerequisito>> buscarPorTipo(
            @PathVariable String tipo) {

        return ResponseEntity.ok(
                prerequisitoService.buscarPorTipo(tipo)
        );
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Prerequisito>> buscarPorEstado(
            @PathVariable String estado) {

        return ResponseEntity.ok(
                prerequisitoService.buscarPorEstado(estado)
        );
    }

    @GetMapping("/buscar")
    public ResponseEntity<Prerequisito> buscarRelacion(
            @RequestParam String cursoId,
            @RequestParam String cursoPrerequisitoId) {

        return prerequisitoService.buscarRelacion(
                        cursoId,
                        cursoPrerequisitoId
                )
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Prerequisito> crear(
            @RequestBody Prerequisito prerequisito) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        prerequisitoService.crear(prerequisito)
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Prerequisito> actualizar(
            @PathVariable String id,
            @RequestBody Prerequisito prerequisito) {

        return ResponseEntity.ok(
                prerequisitoService.actualizar(
                        id,
                        prerequisito
                )
        );
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Prerequisito> cambiarEstado(
            @PathVariable String id,
            @RequestParam String estado) {

        return ResponseEntity.ok(
                prerequisitoService.cambiarEstado(
                        id,
                        estado
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String id) {

        prerequisitoService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}