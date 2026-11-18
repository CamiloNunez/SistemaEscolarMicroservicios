package com.thales_mileto.reportes.repository;

import com.thales_mileto.reportes.model.EventoAuditoria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventoAuditoriaRepository
        extends MongoRepository<EventoAuditoria, String> {

    List<EventoAuditoria> findByUsuarioId(String usuarioId);

    List<EventoAuditoria> findByMicroservicio(String microservicio);

    List<EventoAuditoria> findByTipoEvento(String tipoEvento);

    List<EventoAuditoria> findByEntidad(String entidad);

    List<EventoAuditoria> findByEntidadId(String entidadId);

    List<EventoAuditoria> findByAccion(String accion);

    List<EventoAuditoria> findByUsuarioIdAndMicroservicio(
            String usuarioId,
            String microservicio
    );

    List<EventoAuditoria> findByEntidadAndEntidadId(
            String entidad,
            String entidadId
    );

    List<EventoAuditoria> findByMicroservicioAndTipoEvento(
            String microservicio,
            String tipoEvento
    );

    List<EventoAuditoria> findByFechaBetween(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<EventoAuditoria> findByMicroservicioAndFechaBetween(
            String microservicio,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<EventoAuditoria> findByUsuarioIdAndFechaBetween(
            String usuarioId,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    Optional<EventoAuditoria> findFirstByCorrelationIdOrderByFechaDesc(
            String correlationId
    );

    List<EventoAuditoria> findByCorrelationId(String correlationId);
}