package com.thales_mileto.pagos.repository;

import com.thales_mileto.pagos.model.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComprobanteRepository
        extends JpaRepository<Comprobante, String> {

    Optional<Comprobante> findByPagoId(String pagoId);

    Optional<Comprobante> findByNumeroComprobante(String numeroComprobante);

    List<Comprobante> findByTipoComprobante(String tipoComprobante);

    List<Comprobante> findByEstado(String estado);

    List<Comprobante> findByTipoComprobanteAndEstado(
            String tipoComprobante,
            String estado
    );

    boolean existsByPagoId(String pagoId);

    boolean existsByNumeroComprobante(String numeroComprobante);
}