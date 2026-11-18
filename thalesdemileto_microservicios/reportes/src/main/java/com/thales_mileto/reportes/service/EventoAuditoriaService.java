package com.thales_mileto.reportes.service;

import com.thales_mileto.reportes.model.EventoAuditoria;
import com.thales_mileto.reportes.repository.EventoAuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventoAuditoriaService {

    private final EventoAuditoriaRepository repository;

    // =========================================================
    // LISTAR
    // =========================================================

    public List<EventoAuditoria> listarTodos() {
        return repository.findAll();
    }

    public Optional<EventoAuditoria> buscarPorId(String id) {
        return repository.findById(id);
    }

    // =========================================================
    // BÚSQUEDAS
    // =========================================================

    public List<EventoAuditoria> buscarPorUsuario(String usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    public List<EventoAuditoria> buscarPorMicroservicio(String microservicio) {
        return repository.findByMicroservicio(microservicio);
    }

    public List<EventoAuditoria> buscarPorTipoEvento(String tipoEvento) {
        return repository.findByTipoEvento(tipoEvento);
    }

    public List<EventoAuditoria> buscarPorEntidad(String entidad) {
        return repository.findByEntidad(entidad);
    }

    public List<EventoAuditoria> buscarPorEntidadId(String entidadId) {
        return repository.findByEntidadId(entidadId);
    }

    public List<EventoAuditoria> buscarPorAccion(String accion) {
        return repository.findByAccion(accion);
    }

    public List<EventoAuditoria> buscarPorUsuarioYMicroservicio(
            String usuarioId,
            String microservicio
    ) {
        return repository.findByUsuarioIdAndMicroservicio(
                usuarioId,
                microservicio
        );
    }

    public List<EventoAuditoria> buscarPorEntidadYEntidadId(
            String entidad,
            String entidadId
    ) {
        return repository.findByEntidadAndEntidadId(
                entidad,
                entidadId
        );
    }

    public List<EventoAuditoria> buscarPorMicroservicioYTipoEvento(
            String microservicio,
            String tipoEvento
    ) {
        return repository.findByMicroservicioAndTipoEvento(
                microservicio,
                tipoEvento
        );
    }

    public List<EventoAuditoria> buscarPorFechas(
            LocalDateTime inicio,
            LocalDateTime fin
    ) {
        validarRangoFechas(inicio, fin);

        return repository.findByFechaBetween(inicio, fin);
    }

    public List<EventoAuditoria> buscarPorMicroservicioYFechas(
            String microservicio,
            LocalDateTime inicio,
            LocalDateTime fin
    ) {
        validarRangoFechas(inicio, fin);

        return repository.findByMicroservicioAndFechaBetween(
                microservicio,
                inicio,
                fin
        );
    }

    public List<EventoAuditoria> buscarPorUsuarioYFechas(
            String usuarioId,
            LocalDateTime inicio,
            LocalDateTime fin
    ) {
        validarRangoFechas(inicio, fin);

        return repository.findByUsuarioIdAndFechaBetween(
                usuarioId,
                inicio,
                fin
        );
    }

    public Optional<EventoAuditoria> buscarUltimoPorCorrelationId(
            String correlationId
    ) {
        return repository.findFirstByCorrelationIdOrderByFechaDesc(
                correlationId
        );
    }

    public List<EventoAuditoria> buscarPorCorrelationId(
            String correlationId
    ) {
        return repository.findByCorrelationId(correlationId);
    }

    // =========================================================
    // GUARDAR
    // =========================================================

    public EventoAuditoria guardar(EventoAuditoria evento) {

        if (evento == null) {
            throw new IllegalArgumentException(
                    "El evento de auditoría es obligatorio"
            );
        }

        if (esVacio(evento.getMicroservicio())) {
            throw new IllegalArgumentException(
                    "El microservicio es obligatorio"
            );
        }

        if (esVacio(evento.getTipoEvento())) {
            throw new IllegalArgumentException(
                    "El tipo de evento es obligatorio"
            );
        }

        if (esVacio(evento.getAccion())) {
            throw new IllegalArgumentException(
                    "La acción es obligatoria"
            );
        }

        if (evento.getFecha() == null) {
            evento.setFecha(LocalDateTime.now());
        }

        evento.setMicroservicio(
                evento.getMicroservicio().trim().toLowerCase()
        );

        evento.setTipoEvento(
                evento.getTipoEvento().trim().toUpperCase()
        );

        evento.setAccion(
                evento.getAccion().trim().toUpperCase()
        );

        return repository.save(evento);
    }

    // =========================================================
    // MÉTODO PRÁCTICO PARA REGISTRAR AUDITORÍA
    // =========================================================

    public EventoAuditoria registrarEvento(
            String usuarioId,
            String microservicio,
            String tipoEvento,
            String entidad,
            String entidadId,
            String accion,
            Map<String, Object> datos,
            String ip,
            String correlationId
    ) {

        EventoAuditoria evento = new EventoAuditoria();

        evento.setUsuarioId(usuarioId);
        evento.setMicroservicio(microservicio);
        evento.setTipoEvento(tipoEvento);
        evento.setEntidad(entidad);
        evento.setEntidadId(entidadId);
        evento.setAccion(accion);
        evento.setDatos(datos);
        evento.setIp(ip);
        evento.setFecha(LocalDateTime.now());
        evento.setCorrelationId(correlationId);

        return guardar(evento);
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================

    public EventoAuditoria actualizar(
            String id,
            EventoAuditoria datos
    ) {

        EventoAuditoria existente = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Evento de auditoría no encontrado"
                        )
                );

        if (datos.getUsuarioId() != null) {
            existente.setUsuarioId(datos.getUsuarioId());
        }

        if (datos.getMicroservicio() != null) {
            existente.setMicroservicio(
                    datos.getMicroservicio().trim().toLowerCase()
            );
        }

        if (datos.getTipoEvento() != null) {
            existente.setTipoEvento(
                    datos.getTipoEvento().trim().toUpperCase()
            );
        }

        if (datos.getEntidad() != null) {
            existente.setEntidad(datos.getEntidad());
        }

        if (datos.getEntidadId() != null) {
            existente.setEntidadId(datos.getEntidadId());
        }

        if (datos.getAccion() != null) {
            existente.setAccion(
                    datos.getAccion().trim().toUpperCase()
            );
        }

        if (datos.getDatos() != null) {
            existente.setDatos(datos.getDatos());
        }

        if (datos.getIp() != null) {
            existente.setIp(datos.getIp());
        }

        if (datos.getCorrelationId() != null) {
            existente.setCorrelationId(datos.getCorrelationId());
        }

        return repository.save(existente);
    }

    // =========================================================
    // ELIMINAR
    // =========================================================

    public void eliminar(String id) {

        if (!repository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Evento de auditoría no encontrado"
            );
        }

        repository.deleteById(id);
    }

    // =========================================================
    // UTILIDADES
    // =========================================================

    private void validarRangoFechas(
            LocalDateTime inicio,
            LocalDateTime fin
    ) {

        if (inicio == null || fin == null) {
            throw new IllegalArgumentException(
                    "La fecha de inicio y fin son obligatorias"
            );
        }

        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException(
                    "La fecha de inicio no puede ser posterior a la fecha final"
            );
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
