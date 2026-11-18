package com.thales_mileto.matricula.service;

import com.thales_mileto.matricula.model.PagoMatricula;
import com.thales_mileto.matricula.repository.PagoMatriculaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PagoMatriculaService {

    private final PagoMatriculaRepository pagoMatriculaRepository;

    public List<PagoMatricula> listar() {
        return pagoMatriculaRepository.findAll();
    }

    public PagoMatricula obtenerPorId(String id) {
        return pagoMatriculaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago de matrícula no encontrado"));
    }

    public PagoMatricula crear(PagoMatricula pago, Principal principal) {
        pago.setId(null);

        if (pago.getNumeroComprobante() != null &&
                pagoMatriculaRepository.existsByNumeroComprobante(pago.getNumeroComprobante())) {
            throw new RuntimeException("Ya existe un pago con ese número de comprobante");
        }

        if (pago.getEstado() == null) {
            pago.setEstado("PENDIENTE");
        }

        pago.setUsuarioRegistro(principal.getName());
        pago.setFechaRegistro(LocalDateTime.now());
        pago.setFechaActualizacion(LocalDateTime.now());

        return pagoMatriculaRepository.save(pago);
    }

    public PagoMatricula actualizar(String id, PagoMatricula datos) {
        PagoMatricula pago = obtenerPorId(id);

        pago.setMatriculaId(datos.getMatriculaId());
        pago.setNumeroComprobante(datos.getNumeroComprobante());
        pago.setConcepto(datos.getConcepto());
        pago.setMonto(datos.getMonto());
        pago.setMoneda(datos.getMoneda());
        pago.setMetodoPago(datos.getMetodoPago());
        pago.setFechaVencimiento(datos.getFechaVencimiento());
        pago.setFechaPago(datos.getFechaPago());
        pago.setReferenciaBancaria(datos.getReferenciaBancaria());
        pago.setEntidadBancaria(datos.getEntidadBancaria());
        pago.setEstado(datos.getEstado());
        pago.setObservaciones(datos.getObservaciones());
        pago.setFechaActualizacion(LocalDateTime.now());

        return pagoMatriculaRepository.save(pago);
    }

    public void eliminar(String id) {
        pagoMatriculaRepository.delete(obtenerPorId(id));
    }
}