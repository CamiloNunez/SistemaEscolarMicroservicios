package com.thales_mileto.academico.repository;

import com.thales_mileto.academico.model.AsignacionDocente;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsignacionDocenteRepository
        extends MongoRepository<AsignacionDocente, String> {

    List<AsignacionDocente> findByDocenteId(String docenteId);

    List<AsignacionDocente> findByCursoId(String cursoId);

    List<AsignacionDocente> findBySeccionId(String seccionId);

    List<AsignacionDocente> findByPeriodoAcademicoId(String periodoAcademicoId);

    List<AsignacionDocente> findByEstado(String estado);

    List<AsignacionDocente> findByDocenteIdAndPeriodoAcademicoId(
            String docenteId,
            String periodoAcademicoId
    );

    List<AsignacionDocente> findByCursoIdAndPeriodoAcademicoId(
            String cursoId,
            String periodoAcademicoId
    );

    List<AsignacionDocente> findBySeccionIdAndPeriodoAcademicoId(
            String seccionId,
            String periodoAcademicoId
    );

    Optional<AsignacionDocente>
    findByDocenteIdAndCursoIdAndSeccionIdAndPeriodoAcademicoId(
            String docenteId,
            String cursoId,
            String seccionId,
            String periodoAcademicoId
    );

    boolean existsByDocenteIdAndCursoIdAndSeccionIdAndPeriodoAcademicoId(
            String docenteId,
            String cursoId,
            String seccionId,
            String periodoAcademicoId
    );
}