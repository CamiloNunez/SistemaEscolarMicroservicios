package com.thales_mileto.academico.controller;

import com.thales_mileto.academico.model.Nota;
import com.thales_mileto.academico.service.NotaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notas")
@CrossOrigin(origins = "*")
public class NotaController {

    private final NotaService notaService;

    public NotaController(NotaService notaService) {
        this.notaService = notaService;
    }

    @GetMapping
    public ResponseEntity<List<Nota>> listarTodos() {

        return ResponseEntity.ok(
                notaService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Nota> buscarPorId(
            @PathVariable String id) {

        return ResponseEntity.ok(
                notaService.buscarPorId(id)
        );
    }

    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<List<Nota>> buscarPorEstudiante(
            @PathVariable String estudianteId) {

        return ResponseEntity.ok(
                notaService.buscarPorEstudiante(estudianteId)
        );
    }

    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<List<Nota>> buscarPorCurso(
            @PathVariable String cursoId) {

        return ResponseEntity.ok(
                notaService.buscarPorCurso(cursoId)
        );
    }

    @GetMapping("/docente/{docenteId}")
    public ResponseEntity<List<Nota>> buscarPorDocente(
            @PathVariable String docenteId) {

        return ResponseEntity.ok(
                notaService.buscarPorDocente(docenteId)
        );
    }

    @GetMapping("/periodo/{periodoAcademicoId}")
    public ResponseEntity<List<Nota>> buscarPorPeriodo(
            @PathVariable String periodoAcademicoId) {

        return ResponseEntity.ok(
                notaService.buscarPorPeriodo(
                        periodoAcademicoId
                )
        );
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Nota>> buscarPorEstado(
            @PathVariable String estado) {

        return ResponseEntity.ok(
                notaService.buscarPorEstado(estado)
        );
    }

    @GetMapping("/tipo/{tipoEvaluacion}")
    public ResponseEntity<List<Nota>> buscarPorTipoEvaluacion(
            @PathVariable String tipoEvaluacion) {

        return ResponseEntity.ok(
                notaService.buscarPorTipoEvaluacion(
                        tipoEvaluacion
                )
        );
    }

    @GetMapping("/estudiante/{estudianteId}/curso/{cursoId}")
    public ResponseEntity<List<Nota>> buscarPorEstudianteYCurso(
            @PathVariable String estudianteId,
            @PathVariable String cursoId) {

        return ResponseEntity.ok(
                notaService.buscarPorEstudianteYCurso(
                        estudianteId,
                        cursoId
                )
        );
    }

    @GetMapping("/estudiante/{estudianteId}/periodo/{periodoAcademicoId}")
    public ResponseEntity<List<Nota>> buscarPorEstudianteYPeriodo(
            @PathVariable String estudianteId,
            @PathVariable String periodoAcademicoId) {

        return ResponseEntity.ok(
                notaService.buscarPorEstudianteYPeriodo(
                        estudianteId,
                        periodoAcademicoId
                )
        );
    }

    @GetMapping("/curso/{cursoId}/periodo/{periodoAcademicoId}")
    public ResponseEntity<List<Nota>> buscarPorCursoYPeriodo(
            @PathVariable String cursoId,
            @PathVariable String periodoAcademicoId) {

        return ResponseEntity.ok(
                notaService.buscarPorCursoYPeriodo(
                        cursoId,
                        periodoAcademicoId
                )
        );
    }

    @GetMapping("/docente/{docenteId}/curso/{cursoId}")
    public ResponseEntity<List<Nota>> buscarPorDocenteYCurso(
            @PathVariable String docenteId,
            @PathVariable String cursoId) {

        return ResponseEntity.ok(
                notaService.buscarPorDocenteYCurso(
                        docenteId,
                        cursoId
                )
        );
    }

    @GetMapping(
            "/estudiante/{estudianteId}/curso/{cursoId}/periodo/{periodoAcademicoId}"
    )
    public ResponseEntity<List<Nota>> buscarPorEstudianteCursoPeriodo(
            @PathVariable String estudianteId,
            @PathVariable String cursoId,
            @PathVariable String periodoAcademicoId) {

        return ResponseEntity.ok(
                notaService.buscarPorEstudianteCursoPeriodo(
                        estudianteId,
                        cursoId,
                        periodoAcademicoId
                )
        );
    }

    @GetMapping(
            "/estudiante/{estudianteId}/curso/{cursoId}/periodo/{periodoAcademicoId}/tipo/{tipoEvaluacion}"
    )
    public ResponseEntity<List<Nota>> buscarPorEstudianteCursoPeriodoTipo(
            @PathVariable String estudianteId,
            @PathVariable String cursoId,
            @PathVariable String periodoAcademicoId,
            @PathVariable String tipoEvaluacion) {

        return ResponseEntity.ok(
                notaService.buscarPorEstudianteCursoPeriodoTipo(
                        estudianteId,
                        cursoId,
                        periodoAcademicoId,
                        tipoEvaluacion
                )
        );
    }

    @PostMapping
    public ResponseEntity<Nota> registrar(
            @RequestBody Nota nota) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notaService.registrar(nota));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Nota> actualizar(
            @PathVariable String id,
            @RequestBody Nota nota) {

        return ResponseEntity.ok(
                notaService.actualizar(id, nota)
        );
    }

    @PatchMapping("/{id}/anular")
    public ResponseEntity<Nota> anular(
            @PathVariable String id) {

        return ResponseEntity.ok(
                notaService.anular(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String id) {

        notaService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}