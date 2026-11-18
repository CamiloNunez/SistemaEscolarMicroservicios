package com.thales_mileto.matricula.service;

import com.thales_mileto.matricula.model.PeriodoAcademico;
import com.thales_mileto.matricula.repository.PeriodoAcademicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PeriodoAcademicoService {

    private final PeriodoAcademicoRepository periodoAcademicoRepository;

    public List<PeriodoAcademico> listar() {
        return periodoAcademicoRepository.findAll();
    }

    public PeriodoAcademico obtenerPorId(String id) {
        return periodoAcademicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Periodo académico no encontrado"));
    }

    public PeriodoAcademico crear(PeriodoAcademico periodo, Principal principal) {
        periodo.setId(null);

        if (periodo.getCodigo() != null && periodoAcademicoRepository.existsByCodigo(periodo.getCodigo())) {
            throw new RuntimeException("Ya existe un periodo académico con ese código");
        }

        periodo.setEstado("PLANIFICADO");
        periodo.setUsuarioRegistro(principal.getName());
        periodo.setFechaRegistro(LocalDateTime.now());
        periodo.setFechaActualizacion(LocalDateTime.now());

        return periodoAcademicoRepository.save(periodo);
    }

    public PeriodoAcademico actualizar(String id, PeriodoAcademico datos) {
        PeriodoAcademico periodo = obtenerPorId(id);

        periodo.setCodigo(datos.getCodigo());
        periodo.setNombre(datos.getNombre());
        periodo.setTipo(datos.getTipo());
        periodo.setFechaInicio(datos.getFechaInicio());
        periodo.setFechaFin(datos.getFechaFin());
        periodo.setInicioMatricula(datos.getInicioMatricula());
        periodo.setFinMatricula(datos.getFinMatricula());
        periodo.setInicioMatriculaExtemporanea(datos.getInicioMatriculaExtemporanea());
        periodo.setFinMatriculaExtemporanea(datos.getFinMatriculaExtemporanea());
        periodo.setEstado(datos.getEstado());
        periodo.setFechaActualizacion(LocalDateTime.now());

        return periodoAcademicoRepository.save(periodo);
    }

    public void eliminar(String id) {
        periodoAcademicoRepository.delete(obtenerPorId(id));
    }
}