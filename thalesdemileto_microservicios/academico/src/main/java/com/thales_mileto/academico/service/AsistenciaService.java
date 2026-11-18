package com.thales_mileto.academico.service;

import com.thales_mileto.academico.model.Asistencia;
import com.thales_mileto.academico.model.EstadoAsistencia;
import com.thales_mileto.academico.repository.AsistenciaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;

    public AsistenciaService(
            AsistenciaRepository asistenciaRepository) {
        this.asistenciaRepository = asistenciaRepository;
    }

    public List<Asistencia> listarTodos() {
        return asistenciaRepository.findAll();
    }

    public Asistencia buscarPorId(String id) {
        return asistenciaRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Asistencia no encontrada"
                        )
                );
    }

    public List<Asistencia> buscarPorEstudiante(
            String estudianteId) {
        return asistenciaRepository
                .findByEstudianteId(estudianteId);
    }

    public List<Asistencia> buscarPorCurso(String cursoId) {
        return asistenciaRepository.findByCursoId(cursoId);
    }

    public List<Asistencia> buscarPorDocente(String docenteId) {
        return asistenciaRepository.findByDocenteId(docenteId);
    }

    public List<Asistencia> buscarPorSeccion(String seccionId) {
        return asistenciaRepository.findBySeccionId(seccionId);
    }

    public List<Asistencia> buscarPorEstado(
            EstadoAsistencia estado) {
        return asistenciaRepository.findByEstado(estado);
    }

    public List<Asistencia> buscarPorEstudianteYCurso(
            String estudianteId,
            String cursoId) {

        return asistenciaRepository
                .findByEstudianteIdAndCursoId(
                        estudianteId,
                        cursoId
                );
    }

    public List<Asistencia> buscarPorCursoYSeccion(
            String cursoId,
            String seccionId) {

        return asistenciaRepository
                .findByCursoIdAndSeccionId(
                        cursoId,
                        seccionId
                );
    }

    public List<Asistencia> buscarPorEstudianteEntreFechas(
            String estudianteId,
            LocalDateTime inicio,
            LocalDateTime fin) {

        return asistenciaRepository
                .findByEstudianteIdAndFechaBetween(
                        estudianteId,
                        inicio,
                        fin
                );
    }

    public List<Asistencia> buscarPorCursoSeccionEntreFechas(
            String cursoId,
            String seccionId,
            LocalDateTime inicio,
            LocalDateTime fin) {

        return asistenciaRepository
                .findByCursoIdAndSeccionIdAndFechaBetween(
                        cursoId,
                        seccionId,
                        inicio,
                        fin
                );
    }

    public List<Asistencia> buscarPorEstudianteCursoEstado(
            String estudianteId,
            String cursoId,
            EstadoAsistencia estado) {

        return asistenciaRepository
                .findByEstudianteIdAndCursoIdAndEstado(
                        estudianteId,
                        cursoId,
                        estado
                );
    }

    public Asistencia registrar(Asistencia asistencia) {

        validarDatos(asistencia);

        LocalDateTime fechaAsistencia =
                asistencia.getFecha() != null
                        ? asistencia.getFecha()
                        : LocalDateTime.now();

        LocalDate dia = fechaAsistencia.toLocalDate();

        LocalDateTime inicioDia = dia.atStartOfDay();
        LocalDateTime finDia = dia
                .plusDays(1)
                .atStartOfDay()
                .minusNanos(1);

        boolean existe = asistenciaRepository
                .existsByEstudianteIdAndCursoIdAndSeccionIdAndFechaBetween(
                        asistencia.getEstudianteId(),
                        asistencia.getCursoId(),
                        asistencia.getSeccionId(),
                        inicioDia,
                        finDia
                );

        if (existe) {
            throw new IllegalArgumentException(
                    "Ya existe una asistencia registrada para este estudiante, curso y sección en la fecha indicada"
            );
        }

        asistencia.setId(null);
        asistencia.setFecha(fechaAsistencia);
        asistencia.setFechaRegistro(LocalDateTime.now());

        return asistenciaRepository.save(asistencia);
    }

    public Asistencia actualizar(
            String id,
            Asistencia datos) {

        Asistencia actual = buscarPorId(id);

        if (datos.getEstado() != null) {
            actual.setEstado(datos.getEstado());
        }

        if (datos.getObservacion() != null) {
            actual.setObservacion(datos.getObservacion());
        }

        if (datos.getFecha() != null) {
            actual.setFecha(datos.getFecha());
        }

        return asistenciaRepository.save(actual);
    }

    public void eliminar(String id) {

        if (!asistenciaRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Asistencia no encontrada"
            );
        }

        asistenciaRepository.deleteById(id);
    }

    private void validarDatos(Asistencia asistencia) {

        if (asistencia == null) {
            throw new IllegalArgumentException(
                    "La asistencia es obligatoria"
            );
        }

        if (asistencia.getEstudianteId() == null ||
                asistencia.getEstudianteId().isBlank()) {
            throw new IllegalArgumentException(
                    "El estudiante es obligatorio"
            );
        }

        if (asistencia.getCursoId() == null ||
                asistencia.getCursoId().isBlank()) {
            throw new IllegalArgumentException(
                    "El curso es obligatorio"
            );
        }

        if (asistencia.getDocenteId() == null ||
                asistencia.getDocenteId().isBlank()) {
            throw new IllegalArgumentException(
                    "El docente es obligatorio"
            );
        }

        if (asistencia.getSeccionId() == null ||
                asistencia.getSeccionId().isBlank()) {
            throw new IllegalArgumentException(
                    "La sección es obligatoria"
            );
        }

        if (asistencia.getEstado() == null) {
            throw new IllegalArgumentException(
                    "El estado de asistencia es obligatorio"
            );
        }
    }
}