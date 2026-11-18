package com.thales_mileto.pagos.controller;

import com.thales_mileto.pagos.model.Pago;
import com.thales_mileto.pagos.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PagoController {

    private final PagoService pagoService;

    @GetMapping
    public ResponseEntity<List<Pago>> listarTodos() {
        return ResponseEntity.ok(
                pagoService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pago> buscarPorId(
            @PathVariable String id
    ) {
        return pagoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/deuda/{deudaId}")
    public ResponseEntity<List<Pago>> buscarPorDeuda(
            @PathVariable String deudaId
    ) {
        return ResponseEntity.ok(
                pagoService.buscarPorDeuda(deudaId)
        );
    }

    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<List<Pago>> buscarPorEstudiante(
            @PathVariable String estudianteId
    ) {
        return ResponseEntity.ok(
                pagoService.buscarPorEstudiante(estudianteId)
        );
    }

    @GetMapping("/matricula/{matriculaId}")
    public ResponseEntity<List<Pago>> buscarPorMatricula(
            @PathVariable String matriculaId
    ) {
        return ResponseEntity.ok(
                pagoService.buscarPorMatricula(matriculaId)
        );
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Pago>> buscarPorEstado(
            @PathVariable String estado
    ) {
        return ResponseEntity.ok(
                pagoService.buscarPorEstado(estado)
        );
    }

    @GetMapping("/metodo/{metodoPago}")
    public ResponseEntity<List<Pago>> buscarPorMetodoPago(
            @PathVariable String metodoPago
    ) {
        return ResponseEntity.ok(
                pagoService.buscarPorMetodoPago(metodoPago)
        );
    }

    @GetMapping("/referencia/{referenciaExterna}")
    public ResponseEntity<Pago> buscarPorReferencia(
            @PathVariable String referenciaExterna
    ) {
        return pagoService
                .buscarPorReferenciaExterna(referenciaExterna)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/estudiante/{estudianteId}/estado/{estado}")
    public ResponseEntity<List<Pago>> buscarEstudianteYEstado(
            @PathVariable String estudianteId,
            @PathVariable String estado
    ) {
        return ResponseEntity.ok(
                pagoService.buscarPorEstudianteYEstado(
                        estudianteId,
                        estado
                )
        );
    }

    @GetMapping("/matricula/{matriculaId}/estado/{estado}")
    public ResponseEntity<List<Pago>> buscarMatriculaYEstado(
            @PathVariable String matriculaId,
            @PathVariable String estado
    ) {
        return ResponseEntity.ok(
                pagoService.buscarPorMatriculaYEstado(
                        matriculaId,
                        estado
                )
        );
    }

    @GetMapping("/deuda/{deudaId}/estado/{estado}")
    public ResponseEntity<List<Pago>> buscarDeudaYEstado(
            @PathVariable String deudaId,
            @PathVariable String estado
    ) {
        return ResponseEntity.ok(
                pagoService.buscarPorDeudaYEstado(
                        deudaId,
                        estado
                )
        );
    }

    @GetMapping("/fechas")
    public ResponseEntity<List<Pago>> buscarPorFechas(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime inicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fin
    ) {
        return ResponseEntity.ok(
                pagoService.buscarPorFechas(
                        inicio,
                        fin
                )
        );
    }

    @GetMapping("/estudiante/{estudianteId}/fechas")
    public ResponseEntity<List<Pago>> buscarEstudianteYFechas(

            @PathVariable String estudianteId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime inicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fin
    ) {
        return ResponseEntity.ok(
                pagoService.buscarPorEstudianteYFechas(
                        estudianteId,
                        inicio,
                        fin
                )
        );
    }

    @PostMapping
    public ResponseEntity<?> registrar(
            @RequestBody Pago pago
    ) {
        try {

            return ResponseEntity.ok(
                    pagoService.registrar(pago)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmar(
            @PathVariable String id
    ) {
        try {

            return ResponseEntity.ok(
                    pagoService.confirmarPago(id)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazar(
            @PathVariable String id
    ) {
        try {

            return ResponseEntity.ok(
                    pagoService.rechazarPago(id)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PatchMapping("/{id}/reembolsar")
    public ResponseEntity<?> reembolsar(
            @PathVariable String id
    ) {
        try {

            return ResponseEntity.ok(
                    pagoService.reembolsarPago(id)
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

            pagoService.eliminar(id);

            return ResponseEntity.ok(
                    "Pago eliminado correctamente."
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }
}