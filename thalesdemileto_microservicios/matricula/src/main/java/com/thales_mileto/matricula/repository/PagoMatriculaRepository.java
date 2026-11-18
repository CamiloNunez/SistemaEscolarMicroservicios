package com.thales_mileto.matricula.repository;

import com.thales_mileto.matricula.model.PagoMatricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoMatriculaRepository extends JpaRepository<PagoMatricula, String> {

    List<PagoMatricula> findByMatriculaId(String matriculaId);

    List<PagoMatricula> findByMatriculaIdAndEstado(String matriculaId, String estado);

    List<PagoMatricula> findByEstado(String estado);

    Optional<PagoMatricula> findByNumeroComprobante(String numeroComprobante);

    boolean existsByNumeroComprobante(String numeroComprobante);
}