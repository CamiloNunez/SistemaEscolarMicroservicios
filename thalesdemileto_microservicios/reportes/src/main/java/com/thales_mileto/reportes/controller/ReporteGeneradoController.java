package com.thales_mileto.reportes.controller;

import com.thales_mileto.reportes.model.ReporteGenerado;
import com.thales_mileto.reportes.service.ReporteGeneradoService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes-generados")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReporteGeneradoController {

    private final ReporteGeneradoService service;

    // =========================================================
    // LISTAR
    // =========================================================

    @GetMapping
    public ResponseEntity<List<ReporteGenerado>> listarTodos() {
        return ResponseEntity.ok(
                service.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReporteGenerado> buscarPorId(
            @PathVariable String id
    ) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================
    // BÚSQUEDAS
    // =========================================================

    @GetMapping("/tipo/{tipoReporte}")
    public ResponseEntity<List<ReporteGenerado>> buscarPorTipoReporte(
            @PathVariable String tipoReporte
    ) {
        return ResponseEntity.ok(
                service.buscarPorTipoReporte(tipoReporte)
        );
    }

    @GetMapping("/usuario/{usuarioSolicitanteId}")
    public ResponseEntity<List<ReporteGenerado>> buscarPorUsuario(
            @PathVariable String usuarioSolicitanteId
    ) {
        return ResponseEntity.ok(
                service.buscarPorUsuario(
                        usuarioSolicitanteId
                )
        );
    }

    @GetMapping("/formato/{formato}")
    public ResponseEntity<List<ReporteGenerado>> buscarPorFormato(
            @PathVariable String formato
    ) {
        return ResponseEntity.ok(
                service.buscarPorFormato(formato)
        );
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ReporteGenerado>> buscarPorEstado(
            @PathVariable String estado
    ) {
        return ResponseEntity.ok(
                service.buscarPorEstado(estado)
        );
    }

    @GetMapping("/tipo/{tipoReporte}/estado/{estado}")
    public ResponseEntity<List<ReporteGenerado>> buscarPorTipoYEstado(
            @PathVariable String tipoReporte,
            @PathVariable String estado
    ) {
        return ResponseEntity.ok(
                service.buscarPorTipoYEstado(
                        tipoReporte,
                        estado
                )
        );
    }

    @GetMapping("/usuario/{usuarioSolicitanteId}/estado/{estado}")
    public ResponseEntity<List<ReporteGenerado>> buscarPorUsuarioYEstado(
            @PathVariable String usuarioSolicitanteId,
            @PathVariable String estado
    ) {
        return ResponseEntity.ok(
                service.buscarPorUsuarioYEstado(
                        usuarioSolicitanteId,
                        estado
                )
        );
    }

    @GetMapping("/usuario/{usuarioSolicitanteId}/tipo/{tipoReporte}")
    public ResponseEntity<List<ReporteGenerado>> buscarPorUsuarioYTipo(
            @PathVariable String usuarioSolicitanteId,
            @PathVariable String tipoReporte
    ) {
        return ResponseEntity.ok(
                service.buscarPorUsuarioYTipo(
                        usuarioSolicitanteId,
                        tipoReporte
                )
        );
    }

    // =========================================================
    // ORDENADOS
    // =========================================================

    @GetMapping("/usuario/{usuarioSolicitanteId}/recientes")
    public ResponseEntity<List<ReporteGenerado>> buscarPorUsuarioOrdenado(
            @PathVariable String usuarioSolicitanteId
    ) {
        return ResponseEntity.ok(
                service.buscarPorUsuarioOrdenado(
                        usuarioSolicitanteId
                )
        );
    }

    @GetMapping("/tipo/{tipoReporte}/recientes")
    public ResponseEntity<List<ReporteGenerado>> buscarPorTipoOrdenado(
            @PathVariable String tipoReporte
    ) {
        return ResponseEntity.ok(
                service.buscarPorTipoOrdenado(
                        tipoReporte
                )
        );
    }

    // =========================================================
    // FECHAS
    // =========================================================

    @GetMapping("/fechas")
    public ResponseEntity<?> buscarPorFechas(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime inicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fin
    ) {
        try {

            return ResponseEntity.ok(
                    service.buscarPorFechas(
                            inicio,
                            fin
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "mensaje",
                            e.getMessage()
                    ));
        }
    }

    @GetMapping("/estado/{estado}/fechas")
    public ResponseEntity<?> buscarPorEstadoYFechas(
            @PathVariable String estado,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime inicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fin
    ) {
        try {

            return ResponseEntity.ok(
                    service.buscarPorEstadoYFechas(
                            estado,
                            inicio,
                            fin
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "mensaje",
                            e.getMessage()
                    ));
        }
    }

    // =========================================================
    // CREAR
    // =========================================================

    @PostMapping
    public ResponseEntity<?> guardar(
            @RequestBody ReporteGenerado reporte
    ) {
        try {

            return ResponseEntity.ok(
                    service.guardar(reporte)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "mensaje",
                            e.getMessage()
                    ));
        }
    }

    // =========================================================
    // SOLICITAR REPORTE
    // =========================================================

    @PostMapping("/solicitar")
    public ResponseEntity<?> solicitarReporte(
            @RequestBody SolicitarReporteRequest request
    ) {
        try {

            ReporteGenerado reporte =
                    service.solicitarReporte(
                            request.getTipoReporte(),
                            request.getUsuarioSolicitanteId(),
                            request.getParametros(),
                            request.getFormato()
                    );

            return ResponseEntity.ok(reporte);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "mensaje",
                            e.getMessage()
                    ));
        }
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable String id,
            @RequestBody ReporteGenerado reporte
    ) {
        try {

            return ResponseEntity.ok(
                    service.actualizar(
                            id,
                            reporte
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "mensaje",
                            e.getMessage()
                    ));
        }
    }

    // =========================================================
    // ESTADOS DEL REPORTE
    // =========================================================

    @PatchMapping("/{id}/generando")
    public ResponseEntity<?> marcarGenerando(
            @PathVariable String id
    ) {
        try {

            return ResponseEntity.ok(
                    service.marcarGenerando(id)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "mensaje",
                            e.getMessage()
                    ));
        }
    }

    @PatchMapping("/{id}/listo")
    public ResponseEntity<?> marcarListo(
            @PathVariable String id,
            @RequestBody MarcarListoRequest request
    ) {
        try {

            return ResponseEntity.ok(
                    service.marcarListo(
                            id,
                            request.getRutaArchivo()
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "mensaje",
                            e.getMessage()
                    ));
        }
    }

    @PatchMapping("/{id}/error")
    public ResponseEntity<?> marcarError(
            @PathVariable String id
    ) {
        try {

            return ResponseEntity.ok(
                    service.marcarError(id)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "mensaje",
                            e.getMessage()
                    ));
        }
    }

    // =========================================================
    // ELIMINAR
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable String id
    ) {
        try {

            service.eliminar(id);

            return ResponseEntity.ok(
                    Map.of(
                            "mensaje",
                            "Reporte eliminado correctamente"
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "mensaje",
                            e.getMessage()
                    ));
        }
    }

    // =========================================================
    // DTO SOLICITAR REPORTE
    // =========================================================

    @Data
    public static class SolicitarReporteRequest {

        private String tipoReporte;
        private String usuarioSolicitanteId;
        private Map<String, Object> parametros;
        private String formato;
    }

    // =========================================================
    // DTO MARCAR LISTO
    // =========================================================

    @Data
    public static class MarcarListoRequest {

        private String rutaArchivo;
    }
}