package com.thales_mileto.pagos.service;

import com.thales_mileto.pagos.model.IntentoPago;
import com.thales_mileto.pagos.repository.IntentoPagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class IntentoPagoService {

    private final IntentoPagoRepository intentoPagoRepository;

    public List<IntentoPago> listarTodos() {
        return intentoPagoRepository.findAll();
    }

    public Optional<IntentoPago> buscarPorId(String id) {
        return intentoPagoRepository.findById(id);
    }

    public List<IntentoPago> buscarPorPago(String pagoId) {
        return intentoPagoRepository.findByPagoId(pagoId);
    }

    public List<IntentoPago> buscarPorDeuda(String deudaId) {
        return intentoPagoRepository.findByDeudaId(deudaId);
    }

    public List<IntentoPago> buscarPorProveedor(String proveedor) {
        return intentoPagoRepository.findByProveedor(proveedor);
    }

    public List<IntentoPago> buscarPorEstado(String estado) {
        return intentoPagoRepository.findByEstado(estado);
    }

    public Optional<IntentoPago> buscarPorPaymentIntentId(
            String paymentIntentId
    ) {
        return intentoPagoRepository.findByPaymentIntentId(
                paymentIntentId
        );
    }

    public List<IntentoPago> buscarPorDeudaYEstado(
            String deudaId,
            String estado
    ) {
        return intentoPagoRepository.findByDeudaIdAndEstado(
                deudaId,
                estado
        );
    }

    public List<IntentoPago> buscarPorProveedorYEstado(
            String proveedor,
            String estado
    ) {
        return intentoPagoRepository.findByProveedorAndEstado(
                proveedor,
                estado
        );
    }

    public List<IntentoPago> buscarPorPagoYEstado(
            String pagoId,
            String estado
    ) {
        return intentoPagoRepository.findByPagoIdAndEstado(
                pagoId,
                estado
        );
    }

    public IntentoPago guardar(IntentoPago intentoPago) {

        if (intentoPago.getPaymentIntentId() != null &&
                intentoPagoRepository.existsByPaymentIntentId(
                        intentoPago.getPaymentIntentId())) {
            throw new IllegalArgumentException(
                    "El paymentIntentId ya se encuentra registrado."
            );
        }

        intentoPago.setFechaIntento(LocalDateTime.now());

        if (intentoPago.getEstado() == null ||
                intentoPago.getEstado().isBlank()) {
            intentoPago.setEstado("CREADO");
        }

        return intentoPagoRepository.save(intentoPago);
    }

    public IntentoPago cambiarEstado(
            String id,
            String estado,
            String mensajeRespuesta
    ) {

        IntentoPago intento = intentoPagoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Intento de pago no encontrado."
                        )
                );

        intento.setEstado(estado);
        intento.setMensajeRespuesta(mensajeRespuesta);

        return intentoPagoRepository.save(intento);
    }

    public IntentoPago marcarProcesando(String id) {
        return cambiarEstado(
                id,
                "PROCESANDO",
                "Pago en proceso."
        );
    }

    public IntentoPago marcarExitoso(
            String id,
            String mensaje
    ) {
        return cambiarEstado(
                id,
                "EXITOSO",
                mensaje
        );
    }

    public IntentoPago marcarFallido(
            String id,
            String mensaje
    ) {
        return cambiarEstado(
                id,
                "FALLIDO",
                mensaje
        );
    }

    public void eliminar(String id) {

        if (!intentoPagoRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Intento de pago no encontrado."
            );
        }

        intentoPagoRepository.deleteById(id);
    }
}