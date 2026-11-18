package com.thales_mileto.notificaciones.service;

import com.thales_mileto.notificaciones.model.DestinatarioNotificacion;
import com.thales_mileto.notificaciones.repository.DestinatarioNotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DestinatarioNotificacionService {

    private final DestinatarioNotificacionRepository destinatarioRepository;

    public List<DestinatarioNotificacion> listarTodos() {
        return destinatarioRepository.findAll();
    }

    public Optional<DestinatarioNotificacion> buscarPorId(String id) {
        return destinatarioRepository.findById(id);
    }

    public List<DestinatarioNotificacion> buscarPorNotificacion(
            String notificacionId
    ) {
        return destinatarioRepository.findByNotificacionId(notificacionId);
    }

    public List<DestinatarioNotificacion> buscarPorUsuario(
            String usuarioId
    ) {
        return destinatarioRepository.findByUsuarioId(usuarioId);
    }

    public List<DestinatarioNotificacion> buscarPorTipoDestinatario(
            String tipoDestinatario
    ) {
        return destinatarioRepository.findByTipoDestinatario(tipoDestinatario);
    }

    public List<DestinatarioNotificacion> buscarPorEstadoEntrega(
            String estadoEntrega
    ) {
        return destinatarioRepository.findByEstadoEntrega(estadoEntrega);
    }

    public List<DestinatarioNotificacion> buscarPorNotificacionYEstado(
            String notificacionId,
            String estadoEntrega
    ) {
        return destinatarioRepository.findByNotificacionIdAndEstadoEntrega(
                notificacionId,
                estadoEntrega
        );
    }

    public List<DestinatarioNotificacion> buscarPorUsuarioYEstado(
            String usuarioId,
            String estadoEntrega
    ) {
        return destinatarioRepository.findByUsuarioIdAndEstadoEntrega(
                usuarioId,
                estadoEntrega
        );
    }

    public List<DestinatarioNotificacion> buscarPorTipoYEstado(
            String tipoDestinatario,
            String estadoEntrega
    ) {
        return destinatarioRepository.findByTipoDestinatarioAndEstadoEntrega(
                tipoDestinatario,
                estadoEntrega
        );
    }

    public DestinatarioNotificacion guardar(
            DestinatarioNotificacion destinatario
    ) {

        if (destinatario.getNotificacionId() == null ||
                destinatario.getNotificacionId().isBlank()) {
            throw new IllegalArgumentException(
                    "El notificacionId es obligatorio."
            );
        }

        if (destinatario.getUsuarioId() == null ||
                destinatario.getUsuarioId().isBlank()) {
            throw new IllegalArgumentException(
                    "El usuarioId es obligatorio."
            );
        }

        if (destinatarioRepository.existsByNotificacionIdAndUsuarioId(
                destinatario.getNotificacionId(),
                destinatario.getUsuarioId()
        )) {
            throw new IllegalArgumentException(
                    "El usuario ya se encuentra registrado como destinatario de esta notificación."
            );
        }

        if (destinatario.getDireccionDestino() == null ||
                destinatario.getDireccionDestino().isBlank()) {
            throw new IllegalArgumentException(
                    "La dirección de destino es obligatoria."
            );
        }

        if (destinatario.getEstadoEntrega() == null ||
                destinatario.getEstadoEntrega().isBlank()) {
            destinatario.setEstadoEntrega("PENDIENTE");
        }

        return destinatarioRepository.save(destinatario);
    }

    public DestinatarioNotificacion actualizar(
            String id,
            DestinatarioNotificacion datos
    ) {

        DestinatarioNotificacion destinatario =
                destinatarioRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Destinatario no encontrado."
                                )
                        );

        destinatario.setNotificacionId(datos.getNotificacionId());
        destinatario.setUsuarioId(datos.getUsuarioId());
        destinatario.setDireccionDestino(datos.getDireccionDestino());
        destinatario.setTipoDestinatario(datos.getTipoDestinatario());
        destinatario.setEstadoEntrega(datos.getEstadoEntrega());
        destinatario.setFechaEntrega(datos.getFechaEntrega());

        return destinatarioRepository.save(destinatario);
    }

    public DestinatarioNotificacion marcarEntregado(
            String id
    ) {

        DestinatarioNotificacion destinatario =
                destinatarioRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Destinatario no encontrado."
                                )
                        );

        destinatario.setEstadoEntrega("ENTREGADO");
        destinatario.setFechaEntrega(LocalDateTime.now());

        return destinatarioRepository.save(destinatario);
    }

    public DestinatarioNotificacion marcarFallido(
            String id
    ) {

        DestinatarioNotificacion destinatario =
                destinatarioRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Destinatario no encontrado."
                                )
                        );

        destinatario.setEstadoEntrega("FALLIDO");

        return destinatarioRepository.save(destinatario);
    }

    public void eliminar(String id) {

        if (!destinatarioRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Destinatario no encontrado."
            );
        }

        destinatarioRepository.deleteById(id);
    }
}
