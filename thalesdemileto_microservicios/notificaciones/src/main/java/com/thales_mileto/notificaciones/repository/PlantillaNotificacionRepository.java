package com.thales_mileto.notificaciones.repository;

import com.thales_mileto.notificaciones.model.CanalNotificacion;
import com.thales_mileto.notificaciones.model.PlantillaNotificacion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlantillaNotificacionRepository
        extends MongoRepository<PlantillaNotificacion, String> {

    Optional<PlantillaNotificacion> findByCodigo(String codigo);

    List<PlantillaNotificacion> findByEvento(String evento);

    List<PlantillaNotificacion> findByCanal(CanalNotificacion canal);

    List<PlantillaNotificacion> findByActivo(Boolean activo);

    List<PlantillaNotificacion> findByEventoAndActivo(
            String evento,
            Boolean activo
    );

    List<PlantillaNotificacion> findByCanalAndActivo(
            CanalNotificacion canal,
            Boolean activo
    );

    Optional<PlantillaNotificacion> findByEventoAndCanalAndActivo(
            String evento,
            CanalNotificacion canal,
            Boolean activo
    );

    boolean existsByCodigo(String codigo);
}