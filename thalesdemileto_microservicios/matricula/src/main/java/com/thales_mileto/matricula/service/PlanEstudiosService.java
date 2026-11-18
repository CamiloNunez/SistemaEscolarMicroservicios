package com.thales_mileto.matricula.service;

import com.thales_mileto.matricula.model.PlanEstudios;
import com.thales_mileto.matricula.repository.PlanEstudiosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PlanEstudiosService {

    private final PlanEstudiosRepository planEstudiosRepository;

    public List<PlanEstudios> listar() {
        return planEstudiosRepository.findAll();
    }

    public PlanEstudios obtenerPorId(String id) {
        return planEstudiosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan de estudios no encontrado"));
    }

    public PlanEstudios crear(PlanEstudios plan, Principal principal) {
        plan.setId(null);

        if (planEstudiosRepository.existsByProgramaAcademicoIdAndGradoIdAndPeriodoAcademicoId(
                plan.getProgramaAcademicoId(),
                plan.getGradoId(),
                plan.getPeriodoAcademicoId()
        )) {
            throw new RuntimeException("Ya existe un plan de estudios para ese programa, grado y periodo");
        }

        if (plan.getEstado() == null) {
            plan.setEstado("BORRADOR");
        }

        plan.setUsuarioRegistro(principal.getName());
        plan.setFechaRegistro(LocalDateTime.now());
        plan.setFechaActualizacion(LocalDateTime.now());

        return planEstudiosRepository.save(plan);
    }

    public PlanEstudios actualizar(String id, PlanEstudios datos) {
        PlanEstudios plan = obtenerPorId(id);

        plan.setProgramaAcademicoId(datos.getProgramaAcademicoId());
        plan.setGradoId(datos.getGradoId());
        plan.setPeriodoAcademicoId(datos.getPeriodoAcademicoId());
        plan.setNombre(datos.getNombre());
        plan.setTotalHorasSemanales(datos.getTotalHorasSemanales());
        plan.setCursos(datos.getCursos());
        plan.setEstado(datos.getEstado());
        plan.setFechaActualizacion(LocalDateTime.now());

        return planEstudiosRepository.save(plan);
    }

    public void eliminar(String id) {
        planEstudiosRepository.delete(obtenerPorId(id));
    }
}