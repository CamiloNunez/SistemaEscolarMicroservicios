package com.thales_mileto.notificaciones.repository;

import com.thales_mileto.notificaciones.model.CanalNotificacion;
import com.thales_mileto.notificaciones.model.Notificacion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificacionRepository
        extends MongoRepository<Notificacion, String> {

    List<Notificacion> findByPlantillaId(String plantillaId);

    List<Notificacion> findByTipoEvento(String tipoEvento);

    List<Notificacion> findByReferenciaId(String referenciaId);

    List<Notificacion> findByCanal(CanalNotificacion canal);

    List<Notificacion> findByEstado(String estado);

    List<Notificacion> findByTipoEventoAndEstado(
            String tipoEvento,
            String estado
    );

    List<Notificacion> findByCanalAndEstado(
            CanalNotificacion canal,
            String estado
    );

    List<Notificacion> findByReferenciaIdAndTipoEvento(
            String referenciaId,
            String tipoEvento
    );

    List<Notificacion> findByFechaCreacionBetween(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<Notificacion> findByEstadoAndFechaCreacionBetween(
            String estado,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    boolean existsByTipoEventoAndReferenciaId(
            String tipoEvento,
            String referenciaId
    );
}