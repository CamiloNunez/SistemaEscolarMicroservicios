package com.thales_mileto.login.repository;

import com.thales_mileto.login.model.Rol;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolRepository extends MongoRepository<Rol, String> {

    // List evita que datos históricos duplicados rompan el CRUD.
    List<Rol> findAllByNombre(String nombre);

    Boolean existsByNombre(String nombre);
}
