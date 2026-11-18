package com.thales_mileto.notificaciones.service;

import com.thales_mileto.notificaciones.model.CanalNotificacion;
import com.thales_mileto.notificaciones.model.Notificacion;
import com.thales_mileto.notificaciones.model.PlantillaNotificacion;
import com.thales_mileto.notificaciones.repository.NotificacionRepository;
import com.thales_mileto.notificaciones.repository.PlantillaNotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final PlantillaNotificacionRepository plantillaRepository;

    public List<Notificacion> listarTodas() {
        return notificacionRepository.findAll();
    }

    public Optional<Notificacion> buscarPorId(
            String id
    ) {
        return notificacionRepository.findById(id);
    }

    public List<Notificacion> buscarPorPlantilla(
            String plantillaId
    ) {
        return notificacionRepository.findByPlantillaId(
                plantillaId
        );
    }

    public List<Notificacion> buscarPorEvento(
            String tipoEvento
    ) {
        return notificacionRepository.findByTipoEvento(
                tipoEvento
        );
    }

    public List<Notificacion> buscarPorReferencia(
            String referenciaId
    ) {
        return notificacionRepository.findByReferenciaId(
                referenciaId
        );
    }

    public List<Notificacion> buscarPorCanal(
            CanalNotificacion canal
    ) {
        return notificacionRepository.findByCanal(
                canal
        );
    }

    public List<Notificacion> buscarPorEstado(
            String estado
    ) {
        return notificacionRepository.findByEstado(
                estado
        );
    }

    public List<Notificacion> buscarPorEventoYEstado(
            String tipoEvento,
            String estado
    ) {
        return notificacionRepository.findByTipoEventoAndEstado(
                tipoEvento,
                estado
        );
    }

    public List<Notificacion> buscarPorCanalYEstado(
            CanalNotificacion canal,
            String estado
    ) {
        return notificacionRepository.findByCanalAndEstado(
                canal,
                estado
        );
    }

    public List<Notificacion> buscarPorReferenciaYEvento(
            String referenciaId,
            String tipoEvento
    ) {
        return notificacionRepository.findByReferenciaIdAndTipoEvento(
                referenciaId,
                tipoEvento
        );
    }

    public List<Notificacion> buscarPorFechas(
            LocalDateTime inicio,
            LocalDateTime fin
    ) {
        return notificacionRepository.findByFechaCreacionBetween(
                inicio,
                fin
        );
    }

    public List<Notificacion> buscarPorEstadoYFechas(
            String estado,
            LocalDateTime inicio,
            LocalDateTime fin
    ) {
        return notificacionRepository.findByEstadoAndFechaCreacionBetween(
                estado,
                inicio,
                fin
        );
    }

    public Notificacion guardar(
            Notificacion notificacion
    ) {

        if (notificacion.getTipoEvento() == null ||
                notificacion.getTipoEvento().isBlank()) {
            throw new IllegalArgumentException(
                    "El tipoEvento es obligatorio."
            );
        }

        if (notificacion.getCanal() == null) {
            throw new IllegalArgumentException(
                    "El canal es obligatorio."
            );
        }

        if (notificacion.getAsunto() == null ||
                notificacion.getAsunto().isBlank()) {
            throw new IllegalArgumentException(
                    "El asunto es obligatorio."
            );
        }

        if (notificacion.getContenido() == null ||
                notificacion.getContenido().isBlank()) {
            throw new IllegalArgumentException(
                    "El contenido es obligatorio."
            );
        }

        if (notificacion.getFechaCreacion() == null) {
            notificacion.setFechaCreacion(
                    LocalDateTime.now()
            );
        }

        if (notificacion.getEstado() == null ||
                notificacion.getEstado().isBlank()) {
            notificacion.setEstado("PENDIENTE");
        }

        return notificacionRepository.save(notificacion);
    }

    public Notificacion crearDesdePlantilla(
            String codigoPlantilla,
            String referenciaId,
            Map<String, String> variables
    ) {

        PlantillaNotificacion plantilla =
                plantillaRepository.findByCodigo(codigoPlantilla)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Plantilla no encontrada: "
                                                + codigoPlantilla
                                )
                        );

        if (!Boolean.TRUE.equals(plantilla.getActivo())) {
            throw new IllegalArgumentException(
                    "La plantilla se encuentra inactiva."
            );
        }

        if (
                referenciaId != null &&
                        notificacionRepository.existsByTipoEventoAndReferenciaId(
                                plantilla.getEvento(),
                                referenciaId
                        )
        ) {
            throw new IllegalArgumentException(
                    "Ya existe una notificación para este evento y referencia."
            );
        }

        String asunto = reemplazarVariables(
                plantilla.getAsunto(),
                variables
        );

        String contenido = reemplazarVariables(
                plantilla.getContenido(),
                variables
        );

        Notificacion notificacion =
                new Notificacion();

        notificacion.setPlantillaId(
                plantilla.getId()
        );

        notificacion.setTipoEvento(
                plantilla.getEvento()
        );

        notificacion.setReferenciaId(
                referenciaId
        );

        notificacion.setCanal(
                plantilla.getCanal()
        );

        notificacion.setAsunto(
                asunto
        );

        notificacion.setContenido(
                contenido
        );

        notificacion.setEstado(
                "PENDIENTE"
        );

        notificacion.setFechaCreacion(
                LocalDateTime.now()
        );

        return notificacionRepository.save(
                notificacion
        );
    }

    public Notificacion actualizar(
            String id,
            Notificacion datos
    ) {

        Notificacion notificacion =
                notificacionRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Notificación no encontrada."
                                )
                        );

        notificacion.setPlantillaId(
                datos.getPlantillaId()
        );

        notificacion.setTipoEvento(
                datos.getTipoEvento()
        );

        notificacion.setReferenciaId(
                datos.getReferenciaId()
        );

        notificacion.setCanal(
                datos.getCanal()
        );

        notificacion.setAsunto(
                datos.getAsunto()
        );

        notificacion.setContenido(
                datos.getContenido()
        );

        notificacion.setEstado(
                datos.getEstado()
        );

        return notificacionRepository.save(
                notificacion
        );
    }

    public Notificacion marcarEnviada(
            String id
    ) {

        Notificacion notificacion =
                notificacionRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Notificación no encontrada."
                                )
                        );

        notificacion.setEstado(
                "ENVIADA"
        );

        notificacion.setFechaEnvio(
                LocalDateTime.now()
        );

        return notificacionRepository.save(
                notificacion
        );
    }

    public Notificacion marcarFallida(
            String id
    ) {

        Notificacion notificacion =
                notificacionRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Notificación no encontrada."
                                )
                        );

        notificacion.setEstado(
                "FALLIDA"
        );

        return notificacionRepository.save(
                notificacion
        );
    }

    public void eliminar(
            String id
    ) {

        if (!notificacionRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Notificación no encontrada."
            );
        }

        notificacionRepository.deleteById(id);
    }

    private String reemplazarVariables(
            String texto,
            Map<String, String> variables
    ) {

        if (texto == null) {
            return null;
        }

        if (variables == null ||
                variables.isEmpty()) {
            return texto;
        }

        String resultado = texto;

        for (Map.Entry<String, String> entry :
                variables.entrySet()) {

            String clave =
                    "{{" + entry.getKey() + "}}";

            String valor =
                    entry.getValue() != null
                            ? entry.getValue()
                            : "";

            resultado =
                    resultado.replace(
                            clave,
                            valor
                    );
        }

        return resultado;
    }
}