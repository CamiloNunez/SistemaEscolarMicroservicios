package com.thales_mileto.pagos.controller;

import com.thales_mileto.pagos.model.Deuda;
import com.thales_mileto.pagos.service.DeudaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/deudas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DeudaController {

    private final DeudaService deudaService;

    @GetMapping
    public ResponseEntity<List<Deuda>> listarTodas() {
        return ResponseEntity.ok(
                deudaService.listarTodas()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Deuda> buscarPorId(
            @PathVariable String id
    ) {
        return deudaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<List<Deuda>> buscarPorEstudiante(
            @PathVariable String estudianteId
    ) {
        return ResponseEntity.ok(
                deudaService.buscarPorEstudiante(estudianteId)
        );
    }

    @GetMapping("/matricula/{matriculaId}")
    public ResponseEntity<List<Deuda>> buscarPorMatricula(
            @PathVariable String matriculaId
    ) {
        return ResponseEntity.ok(
                deudaService.buscarPorMatricula(matriculaId)
        );
    }

    @GetMapping("/concepto/{concepto}")
    public ResponseEntity<List<Deuda>> buscarPorConcepto(
            @PathVariable String concepto
    ) {
        return ResponseEntity.ok(
                deudaService.buscarPorConcepto(concepto)
        );
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Deuda>> buscarPorEstado(
            @PathVariable String estado
    ) {
        return ResponseEntity.ok(
                deudaService.buscarPorEstado(estado)
        );
    }

    @GetMapping("/estudiante/{estudianteId}/estado/{estado}")
    public ResponseEntity<List<Deuda>> buscarPorEstudianteYEstado(
            @PathVariable String estudianteId,
            @PathVariable String estado
    ) {
        return ResponseEntity.ok(
                deudaService.buscarPorEstudianteYEstado(
                        estudianteId,
                        estado
                )
        );
    }

    @GetMapping("/matricula/{matriculaId}/estado/{estado}")
    public ResponseEntity<List<Deuda>> buscarPorMatriculaYEstado(
            @PathVariable String matriculaId,
            @PathVariable String estado
    ) {
        return ResponseEntity.ok(
                deudaService.buscarPorMatriculaYEstado(
                        matriculaId,
                        estado
                )
        );
    }

    @GetMapping("/estudiante/{estudianteId}/concepto/{concepto}")
    public ResponseEntity<List<Deuda>> buscarPorEstudianteYConcepto(
            @PathVariable String estudianteId,
            @PathVariable String concepto
    ) {
        return ResponseEntity.ok(
                deudaService.buscarPorEstudianteYConcepto(
                        estudianteId,
                        concepto
                )
        );
    }

    @GetMapping("/vencidas")
    public ResponseEntity<List<Deuda>> listarVencidas() {
        return ResponseEntity.ok(
                deudaService.listarVencidas()
        );
    }

    @GetMapping("/vencidas/pendientes")
    public ResponseEntity<List<Deuda>> listarPendientesVencidas() {
        return ResponseEntity.ok(
                deudaService.listarPendientesVencidas()
        );
    }

    @PostMapping
    public ResponseEntity<?> guardar(
            @RequestBody Deuda deuda
    ) {
        try {

            return ResponseEntity.ok(
                    deudaService.guardar(deuda)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable String id,
            @RequestBody Deuda deuda
    ) {
        try {

            return ResponseEntity.ok(
                    deudaService.actualizar(id, deuda)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PatchMapping("/{id}/saldo")
    public ResponseEntity<?> actualizarSaldo(
            @PathVariable String id,
            @RequestParam BigDecimal monto
    ) {
        try {

            return ResponseEntity.ok(
                    deudaService.actualizarSaldo(
                            id,
                            monto
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PatchMapping("/{id}/anular")
    public ResponseEntity<?> anular(
            @PathVariable String id
    ) {
        try {

            return ResponseEntity.ok(
                    deudaService.anular(id)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable String id
    ) {
        try {

            deudaService.eliminar(id);

            return ResponseEntity.ok(
                    "Deuda eliminada correctamente."
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }
}