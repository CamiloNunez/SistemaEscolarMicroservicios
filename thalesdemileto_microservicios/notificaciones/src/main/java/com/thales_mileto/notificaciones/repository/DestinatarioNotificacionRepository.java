package com.thales_mileto.notificaciones.repository;

import com.thales_mileto.notificaciones.model.DestinatarioNotificacion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DestinatarioNotificacionRepository
        extends MongoRepository<DestinatarioNotificacion, String> {

    List<DestinatarioNotificacion> findByNotificacionId(String notificacionId);

    List<DestinatarioNotificacion> findByUsuarioId(String usuarioId);

    List<DestinatarioNotificacion> findByTipoDestinatario(String tipoDestinatario);

    List<DestinatarioNotificacion> findByEstadoEntrega(String estadoEntrega);

    List<DestinatarioNotificacion> findByNotificacionIdAndEstadoEntrega(
            String notificacionId,
            String estadoEntrega
    );

    List<DestinatarioNotificacion> findByUsuarioIdAndEstadoEntrega(
            String usuarioId,
            String estadoEntrega
    );

    List<DestinatarioNotificacion> findByTipoDestinatarioAndEstadoEntrega(
            String tipoDestinatario,
            String estadoEntrega
    );

    boolean existsByNotificacionIdAndUsuarioId(
            String notificacionId,
            String usuarioId
    );
}