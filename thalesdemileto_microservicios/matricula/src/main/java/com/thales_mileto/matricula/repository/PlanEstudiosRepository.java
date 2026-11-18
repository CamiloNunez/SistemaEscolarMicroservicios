package com.thales_mileto.matricula.repository;

import com.thales_mileto.matricula.model.PlanEstudios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanEstudiosRepository extends JpaRepository<PlanEstudios, String> {

    List<PlanEstudios> findByProgramaAcademicoId(String programaAcademicoId);

    List<PlanEstudios> findByGradoId(String gradoId);

    List<PlanEstudios> findByPeriodoAcademicoId(String periodoAcademicoId);

    Optional<PlanEstudios> findByProgramaAcademicoIdAndGradoIdAndPeriodoAcademicoId(
            String programaAcademicoId,
            String gradoId,
            String periodoAcademicoId
    );

    boolean existsByProgramaAcademicoIdAndGradoIdAndPeriodoAcademicoId(
            String programaAcademicoId,
            String gradoId,
            String periodoAcademicoId
    );

    List<PlanEstudios> findByEstado(String estado);
}