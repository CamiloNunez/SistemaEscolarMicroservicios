package com.thales_mileto.matricula.repository;

import com.thales_mileto.matricula.model.ProgramaAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramaAcademicoRepository extends JpaRepository<ProgramaAcademico, String> {

    Optional<ProgramaAcademico> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<ProgramaAcademico> findByEstado(String estado);

    List<ProgramaAcademico> findByNivelEducativo(String nivelEducativo);
}