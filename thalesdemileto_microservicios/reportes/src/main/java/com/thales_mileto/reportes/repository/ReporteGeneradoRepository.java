package com.thales_mileto.reportes.repository;

import com.thales_mileto.reportes.model.ReporteGenerado;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReporteGeneradoRepository
        extends MongoRepository<ReporteGenerado, String> {

    List<ReporteGenerado> findByTipoReporte(String tipoReporte);

    List<ReporteGenerado> findByUsuarioSolicitanteId(
            String usuarioSolicitanteId
    );

    List<ReporteGenerado> findByFormato(String formato);

    List<ReporteGenerado> findByEstado(String estado);

    List<ReporteGenerado> findByTipoReporteAndEstado(
            String tipoReporte,
            String estado
    );

    List<ReporteGenerado> findByUsuarioSolicitanteIdAndEstado(
            String usuarioSolicitanteId,
            String estado
    );

    List<ReporteGenerado> findByUsuarioSolicitanteIdAndTipoReporte(
            String usuarioSolicitanteId,
            String tipoReporte
    );

    List<ReporteGenerado> findByFechaSolicitudBetween(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<ReporteGenerado> findByEstadoAndFechaSolicitudBetween(
            String estado,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<ReporteGenerado> findByUsuarioSolicitanteIdOrderByFechaSolicitudDesc(
            String usuarioSolicitanteId
    );

    List<ReporteGenerado> findByTipoReporteOrderByFechaSolicitudDesc(
            String tipoReporte
    );
}