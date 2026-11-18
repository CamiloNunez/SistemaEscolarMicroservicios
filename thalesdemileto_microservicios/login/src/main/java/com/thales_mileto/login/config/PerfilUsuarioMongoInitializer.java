package com.thales_mileto.login.config;

import com.thales_mileto.login.model.PerfilUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PerfilUsuarioMongoInitializer implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) {
        try {
            if (!mongoTemplate.collectionExists(PerfilUsuario.class)) {
                mongoTemplate.createCollection(PerfilUsuario.class);
            }

            mongoTemplate.indexOps(PerfilUsuario.class).ensureIndex(
                    new Index().on("usuarioId", Sort.Direction.ASC).unique()
            );

            mongoTemplate.indexOps(PerfilUsuario.class).ensureIndex(
                    new Index().on("entidadId", Sort.Direction.ASC)
            );

            mongoTemplate.indexOps(PerfilUsuario.class).ensureIndex(
                    new Index().on("tipoUsuario", Sort.Direction.ASC)
            );
        } catch (Exception e) {
            System.out.println("No se pudo inicializar perfiles_usuario: " + e.getMessage());
        }

        // Índice separado: si hay duplicados históricos (misma entidad vinculada a
        // dos usuarios) fallará, pero no debe impedir crear los índices anteriores.
        // La regla se valida igualmente en PerfilUsuarioService.
        try {
            mongoTemplate.indexOps(PerfilUsuario.class).ensureIndex(
                    new Index()
                            .on("tipoUsuario", Sort.Direction.ASC)
                            .on("entidadId", Sort.Direction.ASC)
                            .unique()
                            .named("uk_tipo_entidad")
            );
        } catch (Exception e) {
            System.out.println("No se pudo crear el índice único tipoUsuario+entidadId "
                    + "(revise entidades vinculadas a más de un usuario): " + e.getMessage());
        }
    }
}
