package com.thales_mileto.notificaciones.service;

import com.thales_mileto.notificaciones.model.CanalNotificacion;
import com.thales_mileto.notificaciones.model.PlantillaNotificacion;
import com.thales_mileto.notificaciones.repository.PlantillaNotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlantillaNotificacionService {

    private final PlantillaNotificacionRepository plantillaRepository;

    public List<PlantillaNotificacion> listarTodas() {
        return plantillaRepository.findAll();
    }

    public Optional<PlantillaNotificacion> buscarPorId(
            String id
    ) {
        return plantillaRepository.findById(id);
    }

    public Optional<PlantillaNotificacion> buscarPorCodigo(
            String codigo
    ) {
        return plantillaRepository.findByCodigo(codigo);
    }

    public List<PlantillaNotificacion> buscarPorEvento(
            String evento
    ) {
        return plantillaRepository.findByEvento(evento);
    }

    public List<PlantillaNotificacion> buscarPorCanal(
            CanalNotificacion canal
    ) {
        return plantillaRepository.findByCanal(canal);
    }

    public List<PlantillaNotificacion> buscarActivas() {
        return plantillaRepository.findByActivo(true);
    }

    public List<PlantillaNotificacion> buscarInactivas() {
        return plantillaRepository.findByActivo(false);
    }

    public List<PlantillaNotificacion> buscarActivasPorEvento(
            String evento
    ) {
        return plantillaRepository.findByEventoAndActivo(
                evento,
                true
        );
    }

    public List<PlantillaNotificacion> buscarActivasPorCanal(
            CanalNotificacion canal
    ) {
        return plantillaRepository.findByCanalAndActivo(
                canal,
                true
        );
    }

    public Optional<PlantillaNotificacion> buscarPlantillaActiva(
            String evento,
            CanalNotificacion canal
    ) {
        return plantillaRepository.findByEventoAndCanalAndActivo(
                evento,
                canal,
                true
        );
    }

    public PlantillaNotificacion guardar(
            PlantillaNotificacion plantilla
    ) {

        if (plantilla.getCodigo() == null ||
                plantilla.getCodigo().isBlank()) {
            throw new IllegalArgumentException(
                    "El código de plantilla es obligatorio."
            );
        }

        if (plantillaRepository.existsByCodigo(
                plantilla.getCodigo())) {
            throw new IllegalArgumentException(
                    "Ya existe una plantilla con este código."
            );
        }

        if (plantilla.getNombre() == null ||
                plantilla.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre es obligatorio."
            );
        }

        if (plantilla.getEvento() == null ||
                plantilla.getEvento().isBlank()) {
            throw new IllegalArgumentException(
                    "El evento es obligatorio."
            );
        }

        if (plantilla.getCanal() == null) {
            throw new IllegalArgumentException(
                    "El canal es obligatorio."
            );
        }

        if (plantilla.getActivo() == null) {
            plantilla.setActivo(true);
        }

        plantilla.setFechaRegistro(LocalDateTime.now());

        return plantillaRepository.save(plantilla);
    }

    public PlantillaNotificacion actualizar(
            String id,
            PlantillaNotificacion datos
    ) {

        PlantillaNotificacion plantilla =
                plantillaRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Plantilla no encontrada."
                                )
                        );

        if (
                datos.getCodigo() != null &&
                        !datos.getCodigo().equals(plantilla.getCodigo()) &&
                        plantillaRepository.existsByCodigo(datos.getCodigo())
        ) {
            throw new IllegalArgumentException(
                    "Ya existe otra plantilla con este código."
            );
        }

        plantilla.setCodigo(datos.getCodigo());
        plantilla.setNombre(datos.getNombre());
        plantilla.setAsunto(datos.getAsunto());
        plantilla.setContenido(datos.getContenido());
        plantilla.setCanal(datos.getCanal());
        plantilla.setEvento(datos.getEvento());
        plantilla.setActivo(datos.getActivo());

        return plantillaRepository.save(plantilla);
    }

    public PlantillaNotificacion activar(
            String id
    ) {

        PlantillaNotificacion plantilla =
                plantillaRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Plantilla no encontrada."
                                )
                        );

        plantilla.setActivo(true);

        return plantillaRepository.save(plantilla);
    }

    public PlantillaNotificacion desactivar(
            String id
    ) {

        PlantillaNotificacion plantilla =
                plantillaRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Plantilla no encontrada."
                                )
                        );

        plantilla.setActivo(false);

        return plantillaRepository.save(plantilla);
    }

    public void eliminar(String id) {

        if (!plantillaRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Plantilla no encontrada."
            );
        }

        plantillaRepository.deleteById(id);
    }
}