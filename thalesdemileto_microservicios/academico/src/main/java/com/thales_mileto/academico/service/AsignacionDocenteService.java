package com.thales_mileto.academico.service;

import com.thales_mileto.academico.model.AsignacionDocente;
import com.thales_mileto.academico.repository.AsignacionDocenteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AsignacionDocenteService {

    private final AsignacionDocenteRepository asignacionDocenteRepository;

    public AsignacionDocenteService(
            AsignacionDocenteRepository asignacionDocenteRepository) {
        this.asignacionDocenteRepository = asignacionDocenteRepository;
    }

    public List<AsignacionDocente> listarTodos() {
        return asignacionDocenteRepository.findAll();
    }

    public Optional<AsignacionDocente> buscarPorId(String id) {
        return asignacionDocenteRepository.findById(id);
    }

    public List<AsignacionDocente> buscarPorDocente(String docenteId) {
        return asignacionDocenteRepository.findByDocenteId(docenteId);
    }

    public List<AsignacionDocente> buscarPorCurso(String cursoId) {
        return asignacionDocenteRepository.findByCursoId(cursoId);
    }

    public List<AsignacionDocente> buscarPorSeccion(String seccionId) {
        return asignacionDocenteRepository.findBySeccionId(seccionId);
    }

    public List<AsignacionDocente> buscarPorPeriodo(String periodoAcademicoId) {
        return asignacionDocenteRepository
                .findByPeriodoAcademicoId(periodoAcademicoId);
    }

    public List<AsignacionDocente> buscarPorEstado(String estado) {
        return asignacionDocenteRepository.findByEstado(estado);
    }

    public List<AsignacionDocente> buscarPorDocenteYPeriodo(
            String docenteId,
            String periodoAcademicoId) {

        return asignacionDocenteRepository
                .findByDocenteIdAndPeriodoAcademicoId(
                        docenteId,
                        periodoAcademicoId
                );
    }

    public List<AsignacionDocente> buscarPorCursoYPeriodo(
            String cursoId,
            String periodoAcademicoId) {

        return asignacionDocenteRepository
                .findByCursoIdAndPeriodoAcademicoId(
                        cursoId,
                        periodoAcademicoId
                );
    }

    public List<AsignacionDocente> buscarPorSeccionYPeriodo(
            String seccionId,
            String periodoAcademicoId) {

        return asignacionDocenteRepository
                .findBySeccionIdAndPeriodoAcademicoId(
                        seccionId,
                        periodoAcademicoId
                );
    }

    public Optional<AsignacionDocente> buscarAsignacion(
            String docenteId,
            String cursoId,
            String seccionId,
            String periodoAcademicoId) {

        return asignacionDocenteRepository
                .findByDocenteIdAndCursoIdAndSeccionIdAndPeriodoAcademicoId(
                        docenteId,
                        cursoId,
                        seccionId,
                        periodoAcademicoId
                );
    }

    public AsignacionDocente crear(AsignacionDocente asignacion) {

        validarDatos(asignacion);

        boolean existe = asignacionDocenteRepository
                .existsByDocenteIdAndCursoIdAndSeccionIdAndPeriodoAcademicoId(
                        asignacion.getDocenteId(),
                        asignacion.getCursoId(),
                        asignacion.getSeccionId(),
                        asignacion.getPeriodoAcademicoId()
                );

        if (existe) {
            throw new IllegalArgumentException(
                    "El docente ya se encuentra asignado al curso, sección y periodo indicados"
            );
        }

        asignacion.setId(null);

        if (asignacion.getEstado() == null ||
                asignacion.getEstado().isBlank()) {
            asignacion.setEstado("ACTIVA");
        }

        asignacion.setFechaAsignacion(LocalDateTime.now());

        return asignacionDocenteRepository.save(asignacion);
    }

    public AsignacionDocente actualizar(
            String id,
            AsignacionDocente datos) {

        validarDatos(datos);

        AsignacionDocente actual = asignacionDocenteRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Asignación docente no encontrada"
                        )
                );

        Optional<AsignacionDocente> duplicado =
                asignacionDocenteRepository
                        .findByDocenteIdAndCursoIdAndSeccionIdAndPeriodoAcademicoId(
                                datos.getDocenteId(),
                                datos.getCursoId(),
                                datos.getSeccionId(),
                                datos.getPeriodoAcademicoId()
                        );

        if (duplicado.isPresent()
                && !duplicado.get().getId().equals(id)) {

            throw new IllegalArgumentException(
                    "Ya existe otra asignación con los mismos datos"
            );
        }

        actual.setDocenteId(datos.getDocenteId());
        actual.setCursoId(datos.getCursoId());
        actual.setSeccionId(datos.getSeccionId());
        actual.setPeriodoAcademicoId(datos.getPeriodoAcademicoId());
        actual.setEstado(datos.getEstado());

        return asignacionDocenteRepository.save(actual);
    }

    public AsignacionDocente cambiarEstado(
            String id,
            String estado) {

        AsignacionDocente asignacion =
                asignacionDocenteRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Asignación docente no encontrada"
                                )
                        );

        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException(
                    "El estado es obligatorio"
            );
        }

        asignacion.setEstado(estado);

        return asignacionDocenteRepository.save(asignacion);
    }

    public void eliminar(String id) {

        if (!asignacionDocenteRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Asignación docente no encontrada"
            );
        }

        asignacionDocenteRepository.deleteById(id);
    }

    private void validarDatos(AsignacionDocente asignacion) {

        if (asignacion == null) {
            throw new IllegalArgumentException(
                    "La asignación es obligatoria"
            );
        }

        if (asignacion.getDocenteId() == null ||
                asignacion.getDocenteId().isBlank()) {
            throw new IllegalArgumentException(
                    "El docente es obligatorio"
            );
        }

        if (asignacion.getCursoId() == null ||
                asignacion.getCursoId().isBlank()) {
            throw new IllegalArgumentException(
                    "El curso es obligatorio"
            );
        }

        if (asignacion.getSeccionId() == null ||
                asignacion.getSeccionId().isBlank()) {
            throw new IllegalArgumentException(
                    "La sección es obligatoria"
            );
        }

        if (asignacion.getPeriodoAcademicoId() == null ||
                asignacion.getPeriodoAcademicoId().isBlank()) {
            throw new IllegalArgumentException(
                    "El periodo académico es obligatorio"
            );
        }
    }
}