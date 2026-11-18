package com.thales_mileto.login.repository;

import com.thales_mileto.login.model.TipoUsuario;
import com.thales_mileto.login.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    // Se usa List para tolerar datos históricos duplicados sin provocar
    // IncorrectResultSizeDataAccessException / NonUniqueResultException.
    List<Usuario> findAllByUsername(String username);

    List<Usuario> findAllByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Boolean existsByRolesId(String rolId);

    List<Usuario> findByTipoUsuario(TipoUsuario tipoUsuario);

    List<Usuario> findByActivoTrue();

    List<Usuario> findByGradoAndSeccion(String grado, String seccion);
}
