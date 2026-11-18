package com.thales_mileto.reportes.controller;

import com.thales_mileto.reportes.model.Indicador;
import com.thales_mileto.reportes.service.IndicadorService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/indicadores")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IndicadorController {

    private final IndicadorService service;

    // =========================================================
    // LISTAR
    // =========================================================

    @GetMapping
    public ResponseEntity<List<Indicador>> listarTodos() {
        return ResponseEntity.ok(
                service.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Indicador> buscarPorId(
            @PathVariable String id
    ) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================
    // BÚSQUEDAS
    // =========================================================

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<List<Indicador>> buscarPorCodigo(
            @PathVariable String codigo
    ) {
        return ResponseEntity.ok(
                service.buscarPorCodigo(codigo)
        );
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Indicador>> buscarPorTipo(
            @PathVariable String tipo
    ) {
        return ResponseEntity.ok(
                service.buscarPorTipo(tipo)
        );
    }

    @GetMapping("/periodo/{periodo}")
    public ResponseEntity<List<Indicador>> buscarPorPeriodo(
            @PathVariable String periodo
    ) {
        return ResponseEntity.ok(
                service.buscarPorPeriodo(periodo)
        );
    }

    @GetMapping("/codigo/{codigo}/periodo/{periodo}")
    public ResponseEntity<List<Indicador>> buscarPorCodigoYPeriodo(
            @PathVariable String codigo,
            @PathVariable String periodo
    ) {
        return ResponseEntity.ok(
                service.buscarPorCodigoYPeriodo(
                        codigo,
                        periodo
                )
        );
    }

    @GetMapping("/tipo/{tipo}/periodo/{periodo}")
    public ResponseEntity<List<Indicador>> buscarPorTipoYPeriodo(
            @PathVariable String tipo,
            @PathVariable String periodo
    ) {
        return ResponseEntity.ok(
                service.buscarPorTipoYPeriodo(
                        tipo,
                        periodo
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

    @GetMapping("/codigo/{codigo}/fechas")
    public ResponseEntity<?> buscarPorCodigoYFechas(
            @PathVariable String codigo,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime inicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fin
    ) {
        try {

            return ResponseEntity.ok(
                    service.buscarPorCodigoYFechas(
                            codigo,
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
    // ÚLTIMOS INDICADORES
    // =========================================================

    @GetMapping("/codigo/{codigo}/ultimo")
    public ResponseEntity<Indicador> obtenerUltimoPorCodigo(
            @PathVariable String codigo
    ) {
        return service.obtenerUltimoPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/codigo/{codigo}/periodo/{periodo}/ultimo")
    public ResponseEntity<Indicador> obtenerUltimoPorCodigoYPeriodo(
            @PathVariable String codigo,
            @PathVariable String periodo
    ) {
        return service
                .obtenerUltimoPorCodigoYPeriodo(
                        codigo,
                        periodo
                )
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================
    // CREAR
    // =========================================================

    @PostMapping
    public ResponseEntity<?> guardar(
            @RequestBody Indicador indicador
    ) {
        try {

            return ResponseEntity.ok(
                    service.guardar(indicador)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "mensaje",
                            e.getMessage()
                    ));
        }
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarIndicador(
            @RequestBody RegistrarIndicadorRequest request
    ) {
        try {

            Indicador indicador =
                    service.registrarIndicador(
                            request.getCodigo(),
                            request.getNombre(),
                            request.getTipo(),
                            request.getValor(),
                            request.getPeriodo()
                    );

            return ResponseEntity.ok(indicador);

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
            @RequestBody Indicador indicador
    ) {
        try {

            return ResponseEntity.ok(
                    service.actualizar(
                            id,
                            indicador
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
                            "Indicador eliminado correctamente"
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
    // DTO
    // =========================================================

    @Data
    public static class RegistrarIndicadorRequest {

        private String codigo;
        private String nombre;
        private String tipo;
        private Double valor;
        private String periodo;
    }
}