package com.thales_mileto.matricula.repository;

import com.thales_mileto.matricula.model.DocumentoMatricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoMatriculaRepository extends JpaRepository<DocumentoMatricula, String> {

    List<DocumentoMatricula> findByMatriculaId(String matriculaId);

    List<DocumentoMatricula> findByMatriculaIdAndEstado(String matriculaId, String estado);

    List<DocumentoMatricula> findByMatriculaIdAndTipoDocumento(String matriculaId, String tipoDocumento);

    boolean existsByMatriculaIdAndTipoDocumento(String matriculaId, String tipoDocumento);
}