package com.thales_mileto.academico.controller;

import com.thales_mileto.academico.model.AsignacionDocente;
import com.thales_mileto.academico.service.AsignacionDocenteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asignaciones-docente")
@CrossOrigin(origins = "*")
public class AsignacionDocenteController {

    private final AsignacionDocenteService asignacionDocenteService;

    public AsignacionDocenteController(
            AsignacionDocenteService asignacionDocenteService) {
        this.asignacionDocenteService = asignacionDocenteService;
    }

    @GetMapping
    public ResponseEntity<List<AsignacionDocente>> listarTodos() {
        return ResponseEntity.ok(
                asignacionDocenteService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AsignacionDocente> buscarPorId(
            @PathVariable String id) {

        return asignacionDocenteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/docente/{docenteId}")
    public ResponseEntity<List<AsignacionDocente>> buscarPorDocente(
            @PathVariable String docenteId) {

        return ResponseEntity.ok(
                asignacionDocenteService
                        .buscarPorDocente(docenteId)
        );
    }

    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<List<AsignacionDocente>> buscarPorCurso(
            @PathVariable String cursoId) {

        return ResponseEntity.ok(
                asignacionDocenteService
                        .buscarPorCurso(cursoId)
        );
    }

    @GetMapping("/seccion/{seccionId}")
    public ResponseEntity<List<AsignacionDocente>> buscarPorSeccion(
            @PathVariable String seccionId) {

        return ResponseEntity.ok(
                asignacionDocenteService
                        .buscarPorSeccion(seccionId)
        );
    }

    @GetMapping("/periodo/{periodoAcademicoId}")
    public ResponseEntity<List<AsignacionDocente>> buscarPorPeriodo(
            @PathVariable String periodoAcademicoId) {

        return ResponseEntity.ok(
                asignacionDocenteService
                        .buscarPorPeriodo(periodoAcademicoId)
        );
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<AsignacionDocente>> buscarPorEstado(
            @PathVariable String estado) {

        return ResponseEntity.ok(
                asignacionDocenteService.buscarPorEstado(estado)
        );
    }

    @GetMapping("/docente/{docenteId}/periodo/{periodoAcademicoId}")
    public ResponseEntity<List<AsignacionDocente>> buscarPorDocenteYPeriodo(
            @PathVariable String docenteId,
            @PathVariable String periodoAcademicoId) {

        return ResponseEntity.ok(
                asignacionDocenteService.buscarPorDocenteYPeriodo(
                        docenteId,
                        periodoAcademicoId
                )
        );
    }

    @GetMapping("/curso/{cursoId}/periodo/{periodoAcademicoId}")
    public ResponseEntity<List<AsignacionDocente>> buscarPorCursoYPeriodo(
            @PathVariable String cursoId,
            @PathVariable String periodoAcademicoId) {

        return ResponseEntity.ok(
                asignacionDocenteService.buscarPorCursoYPeriodo(
                        cursoId,
                        periodoAcademicoId
                )
        );
    }

    @GetMapping("/seccion/{seccionId}/periodo/{periodoAcademicoId}")
    public ResponseEntity<List<AsignacionDocente>> buscarPorSeccionYPeriodo(
            @PathVariable String seccionId,
            @PathVariable String periodoAcademicoId) {

        return ResponseEntity.ok(
                asignacionDocenteService.buscarPorSeccionYPeriodo(
                        seccionId,
                        periodoAcademicoId
                )
        );
    }

    @GetMapping("/buscar")
    public ResponseEntity<AsignacionDocente> buscarAsignacion(
            @RequestParam String docenteId,
            @RequestParam String cursoId,
            @RequestParam String seccionId,
            @RequestParam String periodoAcademicoId) {

        return asignacionDocenteService.buscarAsignacion(
                        docenteId,
                        cursoId,
                        seccionId,
                        periodoAcademicoId
                )
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AsignacionDocente> crear(
            @RequestBody AsignacionDocente asignacion) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(asignacionDocenteService.crear(asignacion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AsignacionDocente> actualizar(
            @PathVariable String id,
            @RequestBody AsignacionDocente asignacion) {

        return ResponseEntity.ok(
                asignacionDocenteService.actualizar(
                        id,
                        asignacion
                )
        );
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<AsignacionDocente> cambiarEstado(
            @PathVariable String id,
            @RequestParam String estado) {

        return ResponseEntity.ok(
                asignacionDocenteService.cambiarEstado(
                        id,
                        estado
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String id) {

        asignacionDocenteService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}