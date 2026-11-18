package com.thales_mileto.login.repository;

import com.thales_mileto.login.model.PerfilUsuario;
import com.thales_mileto.login.model.TipoUsuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerfilUsuarioRepository extends MongoRepository<PerfilUsuario, String> {
    Optional<PerfilUsuario> findByUsuarioId(String usuarioId);
    Optional<PerfilUsuario> findByEntidadId(String entidadId);
    List<PerfilUsuario> findAllByTipoUsuarioAndEntidadId(TipoUsuario tipoUsuario, String entidadId);
    List<PerfilUsuario> findByTipoUsuario(TipoUsuario tipoUsuario);
    boolean existsByUsuarioId(String usuarioId);
}
