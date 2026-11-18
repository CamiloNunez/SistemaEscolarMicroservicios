package com.thales_mileto.matricula.repository;

import com.thales_mileto.matricula.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CursoRepository extends JpaRepository<Curso, String> {

    Optional<Curso> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Curso> findByEstado(String estado);

    List<Curso> findByArea(String area);

    List<Curso> findByTipo(String tipo);
}