package com.thales_mileto.notificaciones.repository;

import com.thales_mileto.notificaciones.model.PreferenciaNotificacion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PreferenciaNotificacionRepository
        extends MongoRepository<PreferenciaNotificacion, String> {

    Optional<PreferenciaNotificacion> findByUsuarioId(String usuarioId);

    boolean existsByUsuarioId(String usuarioId);
}