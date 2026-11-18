package com.thales_mileto.pagos.controller;

import com.thales_mileto.pagos.model.IntentoPago;
import com.thales_mileto.pagos.service.IntentoPagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/intentos-pago")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IntentoPagoController {

    private final IntentoPagoService intentoPagoService;

    @GetMapping
    public ResponseEntity<List<IntentoPago>> listarTodos() {
        return ResponseEntity.ok(
                intentoPagoService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<IntentoPago> buscarPorId(
            @PathVariable String id
    ) {
        return intentoPagoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pago/{pagoId}")
    public ResponseEntity<List<IntentoPago>> buscarPorPago(
            @PathVariable String pagoId
    ) {
        return ResponseEntity.ok(
                intentoPagoService.buscarPorPago(pagoId)
        );
    }

    @GetMapping("/deuda/{deudaId}")
    public ResponseEntity<List<IntentoPago>> buscarPorDeuda(
            @PathVariable String deudaId
    ) {
        return ResponseEntity.ok(
                intentoPagoService.buscarPorDeuda(deudaId)
        );
    }

    @GetMapping("/proveedor/{proveedor}")
    public ResponseEntity<List<IntentoPago>> buscarPorProveedor(
            @PathVariable String proveedor
    ) {
        return ResponseEntity.ok(
                intentoPagoService.buscarPorProveedor(proveedor)
        );
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<IntentoPago>> buscarPorEstado(
            @PathVariable String estado
    ) {
        return ResponseEntity.ok(
                intentoPagoService.buscarPorEstado(estado)
        );
    }

    @GetMapping("/payment-intent/{paymentIntentId}")
    public ResponseEntity<IntentoPago> buscarPaymentIntent(
            @PathVariable String paymentIntentId
    ) {
        return intentoPagoService
                .buscarPorPaymentIntentId(paymentIntentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/deuda/{deudaId}/estado/{estado}")
    public ResponseEntity<List<IntentoPago>> buscarPorDeudaYEstado(
            @PathVariable String deudaId,
            @PathVariable String estado
    ) {
        return ResponseEntity.ok(
                intentoPagoService.buscarPorDeudaYEstado(
                        deudaId,
                        estado
                )
        );
    }

    @GetMapping("/proveedor/{proveedor}/estado/{estado}")
    public ResponseEntity<List<IntentoPago>> buscarProveedorYEstado(
            @PathVariable String proveedor,
            @PathVariable String estado
    ) {
        return ResponseEntity.ok(
                intentoPagoService.buscarPorProveedorYEstado(
                        proveedor,
                        estado
                )
        );
    }

    @PostMapping
    public ResponseEntity<?> guardar(
            @RequestBody IntentoPago intentoPago
    ) {
        try {

            return ResponseEntity.ok(
                    intentoPagoService.guardar(intentoPago)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PatchMapping("/{id}/procesando")
    public ResponseEntity<?> marcarProcesando(
            @PathVariable String id
    ) {
        try {

            return ResponseEntity.ok(
                    intentoPagoService.marcarProcesando(id)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PatchMapping("/{id}/exitoso")
    public ResponseEntity<?> marcarExitoso(
            @PathVariable String id,
            @RequestParam(required = false)
            String mensaje
    ) {
        try {

            return ResponseEntity.ok(
                    intentoPagoService.marcarExitoso(
                            id,
                            mensaje
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }

    @PatchMapping("/{id}/fallido")
    public ResponseEntity<?> marcarFallido(
            @PathVariable String id,
            @RequestParam(required = false)
            String mensaje
    ) {
        try {

            return ResponseEntity.ok(
                    intentoPagoService.marcarFallido(
                            id,
                            mensaje
                    )
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

            intentoPagoService.eliminar(id);

            return ResponseEntity.ok(
                    "Intento de pago eliminado correctamente."
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }
}