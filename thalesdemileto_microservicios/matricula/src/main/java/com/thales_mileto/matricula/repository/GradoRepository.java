package com.thales_mileto.matricula.repository;

import com.thales_mileto.matricula.model.Grado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradoRepository extends JpaRepository<Grado, String> {

    List<Grado> findByProgramaAcademicoId(String programaAcademicoId);

    Optional<Grado> findByProgramaAcademicoIdAndNumero(String programaAcademicoId, Integer numero);

    boolean existsByProgramaAcademicoIdAndNumero(String programaAcademicoId, Integer numero);

    List<Grado> findByEstado(String estado);
}