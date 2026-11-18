package com.thales_mileto.academico.repository;

import com.thales_mileto.academico.model.Prerequisito;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrerequisitoRepository
        extends MongoRepository<Prerequisito, String> {

    List<Prerequisito> findByCursoId(String cursoId);

    List<Prerequisito> findByCursoPrerequisitoId(String cursoPrerequisitoId);

    List<Prerequisito> findByCursoIdAndEstado(
            String cursoId,
            String estado
    );

    List<Prerequisito> findByTipo(String tipo);

    List<Prerequisito> findByEstado(String estado);

    Optional<Prerequisito> findByCursoIdAndCursoPrerequisitoId(
            String cursoId,
            String cursoPrerequisitoId
    );

    boolean existsByCursoIdAndCursoPrerequisitoId(
            String cursoId,
            String cursoPrerequisitoId
    );
}
