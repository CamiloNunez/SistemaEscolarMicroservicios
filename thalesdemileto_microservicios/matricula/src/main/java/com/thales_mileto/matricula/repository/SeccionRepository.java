package com.thales_mileto.matricula.repository;

import com.thales_mileto.matricula.model.Seccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeccionRepository extends JpaRepository<Seccion, String> {

    List<Seccion> findByGradoId(String gradoId);

    List<Seccion> findByPeriodoAcademicoId(String periodoAcademicoId);

    List<Seccion> findByGradoIdAndPeriodoAcademicoId(String gradoId, String periodoAcademicoId);

    Optional<Seccion> findByGradoIdAndLetraAndPeriodoAcademicoId(
            String gradoId,
            String letra,
            String periodoAcademicoId
    );

    boolean existsByGradoIdAndLetraAndPeriodoAcademicoId(
            String gradoId,
            String letra,
            String periodoAcademicoId
    );

    List<Seccion> findByEstado(String estado);
}