package com.thales_mileto.reportes.service;

import com.thales_mileto.reportes.model.ReporteGenerado;
import com.thales_mileto.reportes.repository.ReporteGeneradoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReporteGeneradoService {

    private final ReporteGeneradoRepository repository;

    // =========================================================
    // LISTAR
    // =========================================================

    public List<ReporteGenerado> listarTodos() {
        return repository.findAll();
    }

    public Optional<ReporteGenerado> buscarPorId(String id) {
        return repository.findById(id);
    }

    // =========================================================
    // BÚSQUEDAS
    // =========================================================

    public List<ReporteGenerado> buscarPorTipoReporte(
            String tipoReporte
    ) {
        return repository.findByTipoReporte(tipoReporte);
    }

    public List<ReporteGenerado> buscarPorUsuario(
            String usuarioSolicitanteId
    ) {
        return repository.findByUsuarioSolicitanteId(
                usuarioSolicitanteId
        );
    }

    public List<ReporteGenerado> buscarPorFormato(
            String formato
    ) {
        return repository.findByFormato(formato);
    }

    public List<ReporteGenerado> buscarPorEstado(
            String estado
    ) {
        return repository.findByEstado(estado);
    }

    public List<ReporteGenerado> buscarPorTipoYEstado(
            String tipoReporte,
            String estado
    ) {
        return repository.findByTipoReporteAndEstado(
                tipoReporte,
                estado
        );
    }

    public List<ReporteGenerado> buscarPorUsuarioYEstado(
            String usuarioSolicitanteId,
            String estado
    ) {
        return repository.findByUsuarioSolicitanteIdAndEstado(
                usuarioSolicitanteId,
                estado
        );
    }

    public List<ReporteGenerado> buscarPorUsuarioYTipo(
            String usuarioSolicitanteId,
            String tipoReporte
    ) {
        return repository.findByUsuarioSolicitanteIdAndTipoReporte(
                usuarioSolicitanteId,
                tipoReporte
        );
    }

    public List<ReporteGenerado> buscarPorFechas(
            LocalDateTime inicio,
            LocalDateTime fin
    ) {

        validarRangoFechas(inicio, fin);

        return repository.findByFechaSolicitudBetween(
                inicio,
                fin
        );
    }

    public List<ReporteGenerado> buscarPorEstadoYFechas(
            String estado,
            LocalDateTime inicio,
            LocalDateTime fin
    ) {

        validarRangoFechas(inicio, fin);

        return repository.findByEstadoAndFechaSolicitudBetween(
                estado,
                inicio,
                fin
        );
    }

    public List<ReporteGenerado> buscarPorUsuarioOrdenado(
            String usuarioSolicitanteId
    ) {
        return repository
                .findByUsuarioSolicitanteIdOrderByFechaSolicitudDesc(
                        usuarioSolicitanteId
                );
    }

    public List<ReporteGenerado> buscarPorTipoOrdenado(
            String tipoReporte
    ) {
        return repository
                .findByTipoReporteOrderByFechaSolicitudDesc(
                        tipoReporte
                );
    }

    // =========================================================
    // SOLICITAR REPORTE
    // =========================================================

    public ReporteGenerado solicitarReporte(
            String tipoReporte,
            String usuarioSolicitanteId,
            Map<String, Object> parametros,
            String formato
    ) {

        ReporteGenerado reporte = new ReporteGenerado();

        reporte.setTipoReporte(tipoReporte);
        reporte.setUsuarioSolicitanteId(usuarioSolicitanteId);
        reporte.setParametros(parametros);
        reporte.setFormato(formato);
        reporte.setEstado("SOLICITADO");
        reporte.setFechaSolicitud(LocalDateTime.now());

        return guardar(reporte);
    }

    // =========================================================
    // GUARDAR
    // =========================================================

    public ReporteGenerado guardar(
            ReporteGenerado reporte
    ) {

        if (reporte == null) {
            throw new IllegalArgumentException(
                    "El reporte es obligatorio"
            );
        }

        validarReporte(reporte);

        if (reporte.getEstado() == null ||
                reporte.getEstado().isBlank()) {
            reporte.setEstado("SOLICITADO");
        }

        if (reporte.getFechaSolicitud() == null) {
            reporte.setFechaSolicitud(
                    LocalDateTime.now()
            );
        }

        reporte.setTipoReporte(
                reporte.getTipoReporte()
                        .trim()
                        .toUpperCase()
        );

        reporte.setFormato(
                reporte.getFormato()
                        .trim()
                        .toUpperCase()
        );

        reporte.setEstado(
                reporte.getEstado()
                        .trim()
                        .toUpperCase()
        );

        return repository.save(reporte);
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================

    public ReporteGenerado actualizar(
            String id,
            ReporteGenerado datos
    ) {

        ReporteGenerado existente = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Reporte no encontrado"
                        )
                );

        if (datos.getTipoReporte() != null) {
            existente.setTipoReporte(
                    datos.getTipoReporte()
                            .trim()
                            .toUpperCase()
            );
        }

        if (datos.getUsuarioSolicitanteId() != null) {
            existente.setUsuarioSolicitanteId(
                    datos.getUsuarioSolicitanteId()
            );
        }

        if (datos.getParametros() != null) {
            existente.setParametros(
                    datos.getParametros()
            );
        }

        if (datos.getFormato() != null) {
            existente.setFormato(
                    datos.getFormato()
                            .trim()
                            .toUpperCase()
            );
        }

        if (datos.getRutaArchivo() != null) {
            existente.setRutaArchivo(
                    datos.getRutaArchivo()
            );
        }

        if (datos.getEstado() != null) {
            existente.setEstado(
                    datos.getEstado()
                            .trim()
                            .toUpperCase()
            );
        }

        validarReporte(existente);

        return repository.save(existente);
    }

    // =========================================================
    // CAMBIAR ESTADOS
    // =========================================================

    public ReporteGenerado marcarGenerando(String id) {

        ReporteGenerado reporte = obtenerReporte(id);

        reporte.setEstado("GENERANDO");

        return repository.save(reporte);
    }

    public ReporteGenerado marcarListo(
            String id,
            String rutaArchivo
    ) {

        if (rutaArchivo == null ||
                rutaArchivo.isBlank()) {
            throw new IllegalArgumentException(
                    "La ruta del archivo generado es obligatoria"
            );
        }

        ReporteGenerado reporte = obtenerReporte(id);

        reporte.setEstado("LISTO");
        reporte.setRutaArchivo(rutaArchivo);
        reporte.setFechaGeneracion(
                LocalDateTime.now()
        );

        return repository.save(reporte);
    }

    public ReporteGenerado marcarError(String id) {

        ReporteGenerado reporte = obtenerReporte(id);

        reporte.setEstado("ERROR");

        return repository.save(reporte);
    }

    // =========================================================
    // ELIMINAR
    // =========================================================

    public void eliminar(String id) {

        if (!repository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Reporte no encontrado"
            );
        }

        repository.deleteById(id);
    }

    // =========================================================
    // UTILIDADES
    // =========================================================

    private ReporteGenerado obtenerReporte(String id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Reporte no encontrado"
                        )
                );
    }

    private void validarReporte(
            ReporteGenerado reporte
    ) {

        if (reporte.getTipoReporte() == null ||
                reporte.getTipoReporte().isBlank()) {
            throw new IllegalArgumentException(
                    "El tipo de reporte es obligatorio"
            );
        }

        if (reporte.getUsuarioSolicitanteId() == null ||
                reporte.getUsuarioSolicitanteId().isBlank()) {
            throw new IllegalArgumentException(
                    "El usuario solicitante es obligatorio"
            );
        }

        if (reporte.getFormato() == null ||
                reporte.getFormato().isBlank()) {
            throw new IllegalArgumentException(
                    "El formato del reporte es obligatorio"
            );
        }

        String formato =
                reporte.getFormato().trim().toUpperCase();

        if (!formato.equals("PDF") &&
                !formato.equals("EXCEL") &&
                !formato.equals("CSV")) {

            throw new IllegalArgumentException(
                    "Formato no válido. Use PDF, EXCEL o CSV"
            );
        }
    }

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
}
