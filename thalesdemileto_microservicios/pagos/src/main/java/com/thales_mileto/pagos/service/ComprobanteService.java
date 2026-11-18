package com.thales_mileto.pagos.service;

import com.thales_mileto.pagos.model.Comprobante;
import com.thales_mileto.pagos.repository.ComprobanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ComprobanteService {

    private final ComprobanteRepository comprobanteRepository;

    public List<Comprobante> listarTodos() {
        return comprobanteRepository.findAll();
    }

    public Optional<Comprobante> buscarPorId(String id) {
        return comprobanteRepository.findById(id);
    }

    public Optional<Comprobante> buscarPorPagoId(String pagoId) {
        return comprobanteRepository.findByPagoId(pagoId);
    }

    public Optional<Comprobante> buscarPorNumeroComprobante(String numeroComprobante) {
        return comprobanteRepository.findByNumeroComprobante(numeroComprobante);
    }

    public List<Comprobante> buscarPorTipo(String tipoComprobante) {
        return comprobanteRepository.findByTipoComprobante(tipoComprobante);
    }

    public List<Comprobante> buscarPorEstado(String estado) {
        return comprobanteRepository.findByEstado(estado);
    }

    public List<Comprobante> buscarPorTipoYEstado(
            String tipoComprobante,
            String estado
    ) {
        return comprobanteRepository.findByTipoComprobanteAndEstado(
                tipoComprobante,
                estado
        );
    }

    public Comprobante guardar(Comprobante comprobante) {

        if (comprobante.getPagoId() == null ||
                comprobante.getPagoId().isBlank()) {
            throw new IllegalArgumentException(
                    "El pagoId es obligatorio."
            );
        }

        if (comprobanteRepository.existsByPagoId(comprobante.getPagoId())) {
            throw new IllegalArgumentException(
                    "Ya existe un comprobante asociado a este pago."
            );
        }

        if (comprobante.getNumeroComprobante() == null ||
                comprobante.getNumeroComprobante().isBlank()) {
            throw new IllegalArgumentException(
                    "El número de comprobante es obligatorio."
            );
        }

        if (comprobanteRepository.existsByNumeroComprobante(
                comprobante.getNumeroComprobante())) {
            throw new IllegalArgumentException(
                    "Ya existe este número de comprobante."
            );
        }

        if (comprobante.getFechaEmision() == null) {
            comprobante.setFechaEmision(LocalDateTime.now());
        }

        if (comprobante.getEstado() == null ||
                comprobante.getEstado().isBlank()) {
            comprobante.setEstado("EMITIDO");
        }

        return comprobanteRepository.save(comprobante);
    }

    public Comprobante actualizar(
            String id,
            Comprobante datos
    ) {

        Comprobante comprobante = comprobanteRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Comprobante no encontrado."
                        )
                );

        comprobante.setPagoId(datos.getPagoId());
        comprobante.setNumeroComprobante(datos.getNumeroComprobante());
        comprobante.setTipoComprobante(datos.getTipoComprobante());
        comprobante.setMonto(datos.getMonto());
        comprobante.setFechaEmision(datos.getFechaEmision());
        comprobante.setUrlDocumento(datos.getUrlDocumento());
        comprobante.setEstado(datos.getEstado());

        return comprobanteRepository.save(comprobante);
    }

    public void eliminar(String id) {
        if (!comprobanteRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Comprobante no encontrado."
            );
        }

        comprobanteRepository.deleteById(id);
    }

    public Comprobante anular(String id) {

        Comprobante comprobante = comprobanteRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Comprobante no encontrado."
                        )
                );

        comprobante.setEstado("ANULADO");

        return comprobanteRepository.save(comprobante);
    }
}