package com.thales_mileto.pagos.repository;

import com.thales_mileto.pagos.model.PasarelaPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasarelaPagoRepository
        extends JpaRepository<PasarelaPago, String> {

    Optional<PasarelaPago> findByProveedor(String proveedor);

    List<PasarelaPago> findByActivo(Boolean activo);

    List<PasarelaPago> findByModo(String modo);

    List<PasarelaPago> findByActivoAndModo(
            Boolean activo,
            String modo
    );

    Optional<PasarelaPago> findByProveedorAndActivo(
            String proveedor,
            Boolean activo
    );

    boolean existsByProveedor(String proveedor);
}