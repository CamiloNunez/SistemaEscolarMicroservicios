package com.thales_mileto.notificaciones.service;

import com.thales_mileto.notificaciones.model.PreferenciaNotificacion;
import com.thales_mileto.notificaciones.repository.PreferenciaNotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PreferenciaNotificacionService {

    private final PreferenciaNotificacionRepository preferenciaRepository;

    public List<PreferenciaNotificacion> listarTodas() {
        return preferenciaRepository.findAll();
    }

    public Optional<PreferenciaNotificacion> buscarPorId(
            String id
    ) {
        return preferenciaRepository.findById(id);
    }

    public Optional<PreferenciaNotificacion> buscarPorUsuario(
            String usuarioId
    ) {
        return preferenciaRepository.findByUsuarioId(usuarioId);
    }

    public PreferenciaNotificacion guardar(
            PreferenciaNotificacion preferencia
    ) {

        if (preferencia.getUsuarioId() == null ||
                preferencia.getUsuarioId().isBlank()) {
            throw new IllegalArgumentException(
                    "El usuarioId es obligatorio."
            );
        }

        if (preferenciaRepository.existsByUsuarioId(
                preferencia.getUsuarioId())) {
            throw new IllegalArgumentException(
                    "El usuario ya tiene preferencias de notificación registradas."
            );
        }

        if (preferencia.getCorreoHabilitado() == null) {
            preferencia.setCorreoHabilitado(true);
        }

        if (preferencia.getPushHabilitado() == null) {
            preferencia.setPushHabilitado(true);
        }

        if (preferencia.getNotificacionesPago() == null) {
            preferencia.setNotificacionesPago(true);
        }

        if (preferencia.getNotificacionesMatricula() == null) {
            preferencia.setNotificacionesMatricula(true);
        }

        if (preferencia.getNotificacionesAcademicas() == null) {
            preferencia.setNotificacionesAcademicas(true);
        }

        preferencia.setFechaActualizacion(
                LocalDateTime.now()
        );

        return preferenciaRepository.save(preferencia);
    }

    public PreferenciaNotificacion obtenerOCrear(
            String usuarioId
    ) {

        return preferenciaRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> {

                    PreferenciaNotificacion nueva =
                            new PreferenciaNotificacion();

                    nueva.setUsuarioId(usuarioId);

                    nueva.setCorreoHabilitado(true);
                    nueva.setPushHabilitado(true);

                    nueva.setNotificacionesPago(true);
                    nueva.setNotificacionesMatricula(true);
                    nueva.setNotificacionesAcademicas(true);

                    nueva.setFechaActualizacion(
                            LocalDateTime.now()
                    );

                    return preferenciaRepository.save(nueva);
                });
    }

    public PreferenciaNotificacion actualizar(
            String usuarioId,
            PreferenciaNotificacion datos
    ) {

        PreferenciaNotificacion preferencia =
                preferenciaRepository
                        .findByUsuarioId(usuarioId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Preferencias del usuario no encontradas."
                                )
                        );

        if (datos.getCorreoHabilitado() != null) {
            preferencia.setCorreoHabilitado(
                    datos.getCorreoHabilitado()
            );
        }

        if (datos.getPushHabilitado() != null) {
            preferencia.setPushHabilitado(
                    datos.getPushHabilitado()
            );
        }

        if (datos.getNotificacionesPago() != null) {
            preferencia.setNotificacionesPago(
                    datos.getNotificacionesPago()
            );
        }

        if (datos.getNotificacionesMatricula() != null) {
            preferencia.setNotificacionesMatricula(
                    datos.getNotificacionesMatricula()
            );
        }

        if (datos.getNotificacionesAcademicas() != null) {
            preferencia.setNotificacionesAcademicas(
                    datos.getNotificacionesAcademicas()
            );
        }

        preferencia.setFechaActualizacion(
                LocalDateTime.now()
        );

        return preferenciaRepository.save(preferencia);
    }

    public void eliminarPorUsuario(
            String usuarioId
    ) {

        PreferenciaNotificacion preferencia =
                preferenciaRepository
                        .findByUsuarioId(usuarioId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Preferencias del usuario no encontradas."
                                )
                        );

        preferenciaRepository.delete(preferencia);
    }
}