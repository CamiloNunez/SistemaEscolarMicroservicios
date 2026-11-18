package com.thales_mileto.academico.service;

import com.thales_mileto.academico.model.Nota;
import com.thales_mileto.academico.repository.NotaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotaService {

    private final NotaRepository notaRepository;

    public NotaService(NotaRepository notaRepository) {
        this.notaRepository = notaRepository;
    }

    public List<Nota> listarTodos() {
        return notaRepository.findAll();
    }

    public Nota buscarPorId(String id) {
        return notaRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Nota no encontrada"
                        )
                );
    }

    public List<Nota> buscarPorEstudiante(String estudianteId) {
        return notaRepository.findByEstudianteId(estudianteId);
    }

    public List<Nota> buscarPorCurso(String cursoId) {
        return notaRepository.findByCursoId(cursoId);
    }

    public List<Nota> buscarPorDocente(String docenteId) {
        return notaRepository.findByDocenteId(docenteId);
    }

    public List<Nota> buscarPorPeriodo(
            String periodoAcademicoId) {

        return notaRepository
                .findByPeriodoAcademicoId(periodoAcademicoId);
    }

    public List<Nota> buscarPorEstado(String estado) {
        return notaRepository.findByEstado(estado);
    }

    public List<Nota> buscarPorTipoEvaluacion(
            String tipoEvaluacion) {

        return notaRepository
                .findByTipoEvaluacion(tipoEvaluacion);
    }

    public List<Nota> buscarPorEstudianteYCurso(
            String estudianteId,
            String cursoId) {

        return notaRepository
                .findByEstudianteIdAndCursoId(
                        estudianteId,
                        cursoId
                );
    }

    public List<Nota> buscarPorEstudianteYPeriodo(
            String estudianteId,
            String periodoAcademicoId) {

        return notaRepository
                .findByEstudianteIdAndPeriodoAcademicoId(
                        estudianteId,
                        periodoAcademicoId
                );
    }

    public List<Nota> buscarPorCursoYPeriodo(
            String cursoId,
            String periodoAcademicoId) {

        return notaRepository
                .findByCursoIdAndPeriodoAcademicoId(
                        cursoId,
                        periodoAcademicoId
                );
    }

    public List<Nota> buscarPorDocenteYCurso(
            String docenteId,
            String cursoId) {

        return notaRepository
                .findByDocenteIdAndCursoId(
                        docenteId,
                        cursoId
                );
    }

    public List<Nota> buscarPorEstudianteCursoPeriodo(
            String estudianteId,
            String cursoId,
            String periodoAcademicoId) {

        return notaRepository
                .findByEstudianteIdAndCursoIdAndPeriodoAcademicoId(
                        estudianteId,
                        cursoId,
                        periodoAcademicoId
                );
    }

    public List<Nota> buscarPorEstudianteCursoPeriodoTipo(
            String estudianteId,
            String cursoId,
            String periodoAcademicoId,
            String tipoEvaluacion) {

        return notaRepository
                .findByEstudianteIdAndCursoIdAndPeriodoAcademicoIdAndTipoEvaluacion(
                        estudianteId,
                        cursoId,
                        periodoAcademicoId,
                        tipoEvaluacion
                );
    }

    public Nota registrar(Nota nota) {

        validarDatos(nota);

        nota.setId(null);

        if (nota.getFechaEvaluacion() == null) {
            nota.setFechaEvaluacion(LocalDateTime.now());
        }

        nota.setFechaRegistro(LocalDateTime.now());

        if (nota.getEstado() == null ||
                nota.getEstado().isBlank()) {
            nota.setEstado("REGISTRADA");
        }

        return notaRepository.save(nota);
    }

    public Nota actualizar(
            String id,
            Nota datos) {

        Nota actual = buscarPorId(id);

        if (datos.getCalificacion() != null) {
            validarCalificacion(datos.getCalificacion());
            actual.setCalificacion(datos.getCalificacion());
        }

        if (datos.getTipoEvaluacion() != null) {
            actual.setTipoEvaluacion(datos.getTipoEvaluacion());
        }

        if (datos.getDescripcion() != null) {
            actual.setDescripcion(datos.getDescripcion());
        }

        if (datos.getFechaEvaluacion() != null) {
            actual.setFechaEvaluacion(datos.getFechaEvaluacion());
        }

        actual.setEstado("MODIFICADA");

        return notaRepository.save(actual);
    }

    public Nota anular(String id) {

        Nota nota = buscarPorId(id);

        nota.setEstado("ANULADA");

        return notaRepository.save(nota);
    }

    public void eliminar(String id) {

        if (!notaRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Nota no encontrada"
            );
        }

        notaRepository.deleteById(id);
    }

    private void validarDatos(Nota nota) {

        if (nota == null) {
            throw new IllegalArgumentException(
                    "La nota es obligatoria"
            );
        }

        if (nota.getEstudianteId() == null ||
                nota.getEstudianteId().isBlank()) {
            throw new IllegalArgumentException(
                    "El estudiante es obligatorio"
            );
        }

        if (nota.getCursoId() == null ||
                nota.getCursoId().isBlank()) {
            throw new IllegalArgumentException(
                    "El curso es obligatorio"
            );
        }

        if (nota.getPeriodoAcademicoId() == null ||
                nota.getPeriodoAcademicoId().isBlank()) {
            throw new IllegalArgumentException(
                    "El periodo académico es obligatorio"
            );
        }

        if (nota.getDocenteId() == null ||
                nota.getDocenteId().isBlank()) {
            throw new IllegalArgumentException(
                    "El docente es obligatorio"
            );
        }

        if (nota.getTipoEvaluacion() == null ||
                nota.getTipoEvaluacion().isBlank()) {
            throw new IllegalArgumentException(
                    "El tipo de evaluación es obligatorio"
            );
        }

        if (nota.getCalificacion() == null) {
            throw new IllegalArgumentException(
                    "La calificación es obligatoria"
            );
        }

        validarCalificacion(nota.getCalificacion());
    }

    private void validarCalificacion(Double calificacion) {

        /*
         * Si tu institución usa otra escala,
         * cambia solamente estos límites.
         */
        if (calificacion < 0 || calificacion > 20) {
            throw new IllegalArgumentException(
                    "La calificación debe estar entre 0 y 20"
            );
        }
    }
}