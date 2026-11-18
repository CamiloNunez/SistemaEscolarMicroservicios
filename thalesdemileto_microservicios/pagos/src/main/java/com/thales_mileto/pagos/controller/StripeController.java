package com.thales_mileto.pagos.controller;

import com.stripe.model.PaymentIntent;
import com.thales_mileto.pagos.dto.StripePagoRequest;
import com.thales_mileto.pagos.dto.StripePagoResponse;
import com.thales_mileto.pagos.model.Deuda;
import com.thales_mileto.pagos.model.Pago;
import com.thales_mileto.pagos.repository.DeudaRepository;
import com.thales_mileto.pagos.service.PagoService;
import com.thales_mileto.pagos.service.StripeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagos/stripe")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StripeController {

    private final DeudaRepository deudaRepository;
    private final PagoService pagoService;
    private final StripeService stripeService;

    @PostMapping("/crear-intento")
    public ResponseEntity<?> crear(
            @RequestBody StripePagoRequest request
    ) {
        try {

            Deuda deuda = deudaRepository.findById(request.getDeudaId())
                    .orElseThrow(() -> new RuntimeException("Deuda no encontrada"));

            Pago pago = new Pago();
            pago.setDeudaId(deuda.getId());
            pago.setMonto(deuda.getSaldoPendiente());
            pago.setMoneda(deuda.getMoneda());
            pago.setMetodoPago("STRIPE");
            pago.setEstado("PENDIENTE");

            pago = pagoService.registrar(pago);

            PaymentIntent intent =
                    stripeService.crearIntento(
                            pago.getMonto(),
                            pago.getMoneda(),
                            pago.getId(),
                            deuda.getId()
                    );

            pago.setReferenciaExterna(intent.getId());
            pagoService.actualizar(pago);

            return ResponseEntity.ok(
                    new StripePagoResponse(
                            pago.getId(),
                            intent.getId(),
                            intent.getClientSecret()
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}