package com.thales_mileto.academico.controller;

import com.thales_mileto.academico.model.Asistencia;
import com.thales_mileto.academico.model.EstadoAsistencia;
import com.thales_mileto.academico.service.AsistenciaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/asistencias")
@CrossOrigin(origins = "*")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    public AsistenciaController(
            AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    @GetMapping
    public ResponseEntity<List<Asistencia>> listarTodos() {

        return ResponseEntity.ok(
                asistenciaService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Asistencia> buscarPorId(
            @PathVariable String id) {

        return ResponseEntity.ok(
                asistenciaService.buscarPorId(id)
        );
    }

    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<List<Asistencia>> buscarPorEstudiante(
            @PathVariable String estudianteId) {

        return ResponseEntity.ok(
                asistenciaService
                        .buscarPorEstudiante(estudianteId)
        );
    }

    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<List<Asistencia>> buscarPorCurso(
            @PathVariable String cursoId) {

        return ResponseEntity.ok(
                asistenciaService.buscarPorCurso(cursoId)
        );
    }

    @GetMapping("/docente/{docenteId}")
    public ResponseEntity<List<Asistencia>> buscarPorDocente(
            @PathVariable String docenteId) {

        return ResponseEntity.ok(
                asistenciaService.buscarPorDocente(docenteId)
        );
    }

    @GetMapping("/seccion/{seccionId}")
    public ResponseEntity<List<Asistencia>> buscarPorSeccion(
            @PathVariable String seccionId) {

        return ResponseEntity.ok(
                asistenciaService.buscarPorSeccion(seccionId)
        );
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Asistencia>> buscarPorEstado(
            @PathVariable EstadoAsistencia estado) {

        return ResponseEntity.ok(
                asistenciaService.buscarPorEstado(estado)
        );
    }

    @GetMapping("/estudiante/{estudianteId}/curso/{cursoId}")
    public ResponseEntity<List<Asistencia>> buscarPorEstudianteYCurso(
            @PathVariable String estudianteId,
            @PathVariable String cursoId) {

        return ResponseEntity.ok(
                asistenciaService.buscarPorEstudianteYCurso(
                        estudianteId,
                        cursoId
                )
        );
    }

    @GetMapping("/curso/{cursoId}/seccion/{seccionId}")
    public ResponseEntity<List<Asistencia>> buscarPorCursoYSeccion(
            @PathVariable String cursoId,
            @PathVariable String seccionId) {

        return ResponseEntity.ok(
                asistenciaService.buscarPorCursoYSeccion(
                        cursoId,
                        seccionId
                )
        );
    }

    @GetMapping("/estudiante/{estudianteId}/fechas")
    public ResponseEntity<List<Asistencia>> buscarPorEstudianteEntreFechas(
            @PathVariable String estudianteId,
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fin) {

        return ResponseEntity.ok(
                asistenciaService.buscarPorEstudianteEntreFechas(
                        estudianteId,
                        inicio,
                        fin
                )
        );
    }

    @GetMapping("/curso/{cursoId}/seccion/{seccionId}/fechas")
    public ResponseEntity<List<Asistencia>> buscarCursoSeccionEntreFechas(
            @PathVariable String cursoId,
            @PathVariable String seccionId,
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fin) {

        return ResponseEntity.ok(
                asistenciaService.buscarPorCursoSeccionEntreFechas(
                        cursoId,
                        seccionId,
                        inicio,
                        fin
                )
        );
    }

    @GetMapping("/estudiante/{estudianteId}/curso/{cursoId}/estado/{estado}")
    public ResponseEntity<List<Asistencia>> buscarPorEstudianteCursoEstado(
            @PathVariable String estudianteId,
            @PathVariable String cursoId,
            @PathVariable EstadoAsistencia estado) {

        return ResponseEntity.ok(
                asistenciaService.buscarPorEstudianteCursoEstado(
                        estudianteId,
                        cursoId,
                        estado
                )
        );
    }

    @PostMapping
    public ResponseEntity<Asistencia> registrar(
            @RequestBody Asistencia asistencia) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        asistenciaService.registrar(asistencia)
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Asistencia> actualizar(
            @PathVariable String id,
            @RequestBody Asistencia asistencia) {

        return ResponseEntity.ok(
                asistenciaService.actualizar(
                        id,
                        asistencia
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String id) {

        asistenciaService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}