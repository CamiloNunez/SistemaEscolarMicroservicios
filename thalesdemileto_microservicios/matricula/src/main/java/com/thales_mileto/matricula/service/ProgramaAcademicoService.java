package com.thales_mileto.matricula.service;

import com.thales_mileto.matricula.model.ProgramaAcademico;
import com.thales_mileto.matricula.repository.ProgramaAcademicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProgramaAcademicoService {

    private final ProgramaAcademicoRepository programaAcademicoRepository;

    public List<ProgramaAcademico> listar() {
        return programaAcademicoRepository.findAll();
    }

    public ProgramaAcademico obtenerPorId(String id) {
        return programaAcademicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Programa académico no encontrado"));
    }

    public ProgramaAcademico crear(ProgramaAcademico programa, Principal principal) {
        programa.setId(null);

        if (programa.getCodigo() != null && programaAcademicoRepository.existsByCodigo(programa.getCodigo())) {
            throw new RuntimeException("Ya existe un programa académico con ese código");
        }

        programa.setEstado("ACTIVO");
        programa.setUsuarioRegistro(principal.getName());
        programa.setFechaRegistro(LocalDateTime.now());
        programa.setFechaActualizacion(LocalDateTime.now());

        return programaAcademicoRepository.save(programa);
    }

    public ProgramaAcademico actualizar(String id, ProgramaAcademico datos) {
        ProgramaAcademico programa = obtenerPorId(id);

        programa.setCodigo(datos.getCodigo());
        programa.setNombre(datos.getNombre());
        programa.setDescripcion(datos.getDescripcion());
        programa.setDuracionAnios(datos.getDuracionAnios());
        programa.setNivelEducativo(datos.getNivelEducativo());
        programa.setEstado(datos.getEstado());
        programa.setFechaActualizacion(LocalDateTime.now());

        return programaAcademicoRepository.save(programa);
    }

    public void eliminar(String id) {
        programaAcademicoRepository.delete(obtenerPorId(id));
    }
}