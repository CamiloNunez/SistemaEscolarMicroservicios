package com.thales_mileto.pagos.service;

import com.thales_mileto.pagos.model.PasarelaPago;
import com.thales_mileto.pagos.repository.PasarelaPagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PasarelaPagoService {

    private final PasarelaPagoRepository pasarelaPagoRepository;

    public List<PasarelaPago> listarTodas() {
        return pasarelaPagoRepository.findAll();
    }

    public Optional<PasarelaPago> buscarPorId(String id) {
        return pasarelaPagoRepository.findById(id);
    }

    public Optional<PasarelaPago> buscarPorProveedor(
            String proveedor
    ) {
        return pasarelaPagoRepository.findByProveedor(proveedor);
    }

    public List<PasarelaPago> buscarActivas() {
        return pasarelaPagoRepository.findByActivo(true);
    }

    public List<PasarelaPago> buscarInactivas() {
        return pasarelaPagoRepository.findByActivo(false);
    }

    public List<PasarelaPago> buscarPorModo(String modo) {
        return pasarelaPagoRepository.findByModo(modo);
    }

    public List<PasarelaPago> buscarActivasPorModo(
            String modo
    ) {
        return pasarelaPagoRepository.findByActivoAndModo(
                true,
                modo
        );
    }

    public Optional<PasarelaPago> buscarProveedorActivo(
            String proveedor
    ) {
        return pasarelaPagoRepository.findByProveedorAndActivo(
                proveedor,
                true
        );
    }

    public PasarelaPago guardar(PasarelaPago pasarela) {

        if (pasarela.getProveedor() == null ||
                pasarela.getProveedor().isBlank()) {
            throw new IllegalArgumentException(
                    "El proveedor es obligatorio."
            );
        }

        if (pasarelaPagoRepository.existsByProveedor(
                pasarela.getProveedor())) {
            throw new IllegalArgumentException(
                    "El proveedor ya se encuentra registrado."
            );
        }

        pasarela.setFechaRegistro(LocalDateTime.now());

        if (pasarela.getActivo() == null) {
            pasarela.setActivo(true);
        }

        if (pasarela.getModo() == null ||
                pasarela.getModo().isBlank()) {
            pasarela.setModo("SANDBOX");
        }

        return pasarelaPagoRepository.save(pasarela);
    }

    public PasarelaPago actualizar(
            String id,
            PasarelaPago datos
    ) {

        PasarelaPago pasarela =
                pasarelaPagoRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Pasarela de pago no encontrada."
                                )
                        );

        pasarela.setNombre(datos.getNombre());
        pasarela.setProveedor(datos.getProveedor());
        pasarela.setActivo(datos.getActivo());
        pasarela.setModo(datos.getModo());

        return pasarelaPagoRepository.save(pasarela);
    }

    public PasarelaPago activar(String id) {

        PasarelaPago pasarela =
                pasarelaPagoRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Pasarela de pago no encontrada."
                                )
                        );

        pasarela.setActivo(true);

        return pasarelaPagoRepository.save(pasarela);
    }

    public PasarelaPago desactivar(String id) {

        PasarelaPago pasarela =
                pasarelaPagoRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Pasarela de pago no encontrada."
                                )
                        );

        pasarela.setActivo(false);

        return pasarelaPagoRepository.save(pasarela);
    }

    public void eliminar(String id) {

        if (!pasarelaPagoRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Pasarela de pago no encontrada."
            );
        }

        pasarelaPagoRepository.deleteById(id);
    }
}