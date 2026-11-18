package com.thales_mileto.matricula.repository;

import com.thales_mileto.matricula.model.PeriodoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeriodoAcademicoRepository extends JpaRepository<PeriodoAcademico, String> {

    Optional<PeriodoAcademico> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<PeriodoAcademico> findByEstado(String estado);

    List<PeriodoAcademico> findByTipo(String tipo);
}