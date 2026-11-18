package com.thales_mileto.academico.repository;

import com.thales_mileto.academico.model.Asistencia;
import com.thales_mileto.academico.model.EstadoAsistencia;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AsistenciaRepository
        extends MongoRepository<Asistencia, String> {

    List<Asistencia> findByEstudianteId(String estudianteId);

    List<Asistencia> findByCursoId(String cursoId);

    List<Asistencia> findByDocenteId(String docenteId);

    List<Asistencia> findBySeccionId(String seccionId);

    List<Asistencia> findByEstado(EstadoAsistencia estado);

    List<Asistencia> findByEstudianteIdAndCursoId(
            String estudianteId,
            String cursoId
    );

    List<Asistencia> findByCursoIdAndSeccionId(
            String cursoId,
            String seccionId
    );

    List<Asistencia> findByEstudianteIdAndFechaBetween(
            String estudianteId,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<Asistencia> findByCursoIdAndSeccionIdAndFechaBetween(
            String cursoId,
            String seccionId,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<Asistencia> findByEstudianteIdAndCursoIdAndEstado(
            String estudianteId,
            String cursoId,
            EstadoAsistencia estado
    );

    boolean existsByEstudianteIdAndCursoIdAndSeccionIdAndFechaBetween(
            String estudianteId,
            String cursoId,
            String seccionId,
            LocalDateTime inicio,
            LocalDateTime fin
    );
}