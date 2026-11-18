package com.thales_mileto.academico.repository;

import com.thales_mileto.academico.model.Nota;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaRepository extends MongoRepository<Nota, String> {

    List<Nota> findByEstudianteId(String estudianteId);

    List<Nota> findByCursoId(String cursoId);

    List<Nota> findByDocenteId(String docenteId);

    List<Nota> findByPeriodoAcademicoId(String periodoAcademicoId);

    List<Nota> findByEstado(String estado);

    List<Nota> findByTipoEvaluacion(String tipoEvaluacion);

    List<Nota> findByEstudianteIdAndCursoId(
            String estudianteId,
            String cursoId
    );

    List<Nota> findByEstudianteIdAndPeriodoAcademicoId(
            String estudianteId,
            String periodoAcademicoId
    );

    List<Nota> findByCursoIdAndPeriodoAcademicoId(
            String cursoId,
            String periodoAcademicoId
    );

    List<Nota> findByDocenteIdAndCursoId(
            String docenteId,
            String cursoId
    );

    List<Nota> findByEstudianteIdAndCursoIdAndPeriodoAcademicoId(
            String estudianteId,
            String cursoId,
            String periodoAcademicoId
    );

    List<Nota> findByEstudianteIdAndCursoIdAndPeriodoAcademicoIdAndTipoEvaluacion(
            String estudianteId,
            String cursoId,
            String periodoAcademicoId,
            String tipoEvaluacion
    );
}