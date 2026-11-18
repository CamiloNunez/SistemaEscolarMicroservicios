package com.thales_mileto.pagos.repository;

import com.thales_mileto.pagos.model.IntentoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntentoPagoRepository
        extends JpaRepository<IntentoPago, String> {

    List<IntentoPago> findByPagoId(String pagoId);

    List<IntentoPago> findByDeudaId(String deudaId);

    List<IntentoPago> findByProveedor(String proveedor);

    List<IntentoPago> findByEstado(String estado);

    Optional<IntentoPago> findByPaymentIntentId(
            String paymentIntentId
    );

    List<IntentoPago> findByDeudaIdAndEstado(
            String deudaId,
            String estado
    );

    List<IntentoPago> findByProveedorAndEstado(
            String proveedor,
            String estado
    );

    List<IntentoPago> findByPagoIdAndEstado(
            String pagoId,
            String estado
    );

    boolean existsByPaymentIntentId(String paymentIntentId);
}