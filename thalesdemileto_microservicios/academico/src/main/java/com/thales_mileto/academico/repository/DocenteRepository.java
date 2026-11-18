package com.thales_mileto.academico.repository;

import com.thales_mileto.academico.model.Docente;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocenteRepository extends MongoRepository<Docente, String> {

    Optional<Docente> findByUsuarioId(String usuarioId);

    Optional<Docente> findByCodigoDocente(String codigoDocente);

    List<Docente> findByEstado(String estado);

    List<Docente> findByEspecialidad(String especialidad);

    boolean existsByUsuarioId(String usuarioId);

    boolean existsByCodigoDocente(String codigoDocente);
}
