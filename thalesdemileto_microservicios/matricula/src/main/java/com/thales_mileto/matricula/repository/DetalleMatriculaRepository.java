package com.thales_mileto.matricula.repository;

import com.thales_mileto.matricula.model.DetalleMatricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DetalleMatriculaRepository extends JpaRepository<DetalleMatricula, String> {

    List<DetalleMatricula> findByMatriculaId(String matriculaId);

    Optional<DetalleMatricula> findByMatriculaIdAndCursoId(String matriculaId, String cursoId);

    boolean existsByMatriculaIdAndCursoId(String matriculaId, String cursoId);

    List<DetalleMatricula> findByMatriculaIdAndEstado(String matriculaId, String estado);
}