package com.thales_mileto.pagos.service;

import com.thales_mileto.pagos.model.Deuda;
import com.thales_mileto.pagos.model.Pago;
import com.thales_mileto.pagos.repository.DeudaRepository;
import com.thales_mileto.pagos.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final DeudaRepository deudaRepository;

    public List<Pago> listarTodos() {
        return pagoRepository.findAll();
    }

    public Optional<Pago> buscarPorId(String id) {
        return pagoRepository.findById(id);
    }

    public List<Pago> buscarPorDeuda(String deudaId) {
        return pagoRepository.findByDeudaId(deudaId);
    }

    public List<Pago> buscarPorEstudiante(String estudianteId) {
        return pagoRepository.findByEstudianteId(estudianteId);
    }

    public List<Pago> buscarPorMatricula(String matriculaId) {
        return pagoRepository.findByMatriculaId(matriculaId);
    }

    public List<Pago> buscarPorEstado(String estado) {
        return pagoRepository.findByEstado(estado);
    }

    public List<Pago> buscarPorMetodoPago(String metodoPago) {
        return pagoRepository.findByMetodoPago(metodoPago);
    }

    public Optional<Pago> buscarPorReferenciaExterna(
            String referenciaExterna
    ) {
        return pagoRepository.findByReferenciaExterna(
                referenciaExterna
        );
    }

    public List<Pago> buscarPorEstudianteYEstado(
            String estudianteId,
            String estado
    ) {
        return pagoRepository.findByEstudianteIdAndEstado(
                estudianteId,
                estado
        );
    }

    public List<Pago> buscarPorMatriculaYEstado(
            String matriculaId,
            String estado
    ) {
        return pagoRepository.findByMatriculaIdAndEstado(
                matriculaId,
                estado
        );
    }

    public List<Pago> buscarPorDeudaYEstado(
            String deudaId,
            String estado
    ) {
        return pagoRepository.findByDeudaIdAndEstado(
                deudaId,
                estado
        );
    }

    public List<Pago> buscarPorMetodoYEstado(
            String metodoPago,
            String estado
    ) {
        return pagoRepository.findByMetodoPagoAndEstado(
                metodoPago,
                estado
        );
    }

    public List<Pago> buscarPorFechas(
            LocalDateTime inicio,
            LocalDateTime fin
    ) {
        return pagoRepository.findByFechaPagoBetween(
                inicio,
                fin
        );
    }

    public List<Pago> buscarPorEstudianteYFechas(
            String estudianteId,
            LocalDateTime inicio,
            LocalDateTime fin
    ) {
        return pagoRepository
                .findByEstudianteIdAndFechaPagoBetween(
                        estudianteId,
                        inicio,
                        fin
                );
    }

    public Pago registrar(Pago pago) {

        if (pago.getMonto() == null ||
                pago.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El monto del pago debe ser mayor a cero."
            );
        }

        if (pago.getDeudaId() == null ||
                pago.getDeudaId().isBlank()) {
            throw new IllegalArgumentException(
                    "El deudaId es obligatorio."
            );
        }

        Deuda deuda = deudaRepository.findByIdForUpdate(pago.getDeudaId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La deuda indicada no existe."
                        )
                );

        if ("PAGADA".equalsIgnoreCase(deuda.getEstado())) {
            throw new IllegalArgumentException(
                    "La deuda ya se encuentra pagada."
            );
        }

        if ("ANULADA".equalsIgnoreCase(deuda.getEstado())) {
            throw new IllegalArgumentException(
                    "No se puede pagar una deuda anulada."
            );
        }

        if (pago.getMonto().compareTo(
                deuda.getSaldoPendiente()) > 0) {
            throw new IllegalArgumentException(
                    "El monto supera el saldo pendiente de la deuda."
            );
        }

        if (pago.getReferenciaExterna() != null &&
                !pago.getReferenciaExterna().isBlank() &&
                pagoRepository.existsByReferenciaExterna(
                        pago.getReferenciaExterna())) {
            throw new IllegalArgumentException(
                    "La referencia externa ya fue registrada."
            );
        }

        if (pago.getMoneda() == null ||
                pago.getMoneda().isBlank()) {
            pago.setMoneda(
                    deuda.getMoneda() != null
                            ? deuda.getMoneda()
                            : "PEN"
            );
        }

        pago.setEstudianteId(deuda.getEstudianteId());

        if (pago.getMatriculaId() == null) {
            pago.setMatriculaId(deuda.getMatriculaId());
        }

        pago.setFechaRegistro(LocalDateTime.now());

        if (pago.getFechaPago() == null) {
            pago.setFechaPago(LocalDateTime.now());
        }

        if (pago.getEstado() == null ||
                pago.getEstado().isBlank()) {
            pago.setEstado("PENDIENTE");
        }

        return pagoRepository.save(pago);
    }

    public Pago actualizar(Pago pago) { return pagoRepository.save(pago); }

    public Pago confirmarPago(String pagoId) {

        Pago pago = pagoRepository.findByIdForUpdate(pagoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Pago no encontrado."
                        )
                );

        if ("CONFIRMADO".equalsIgnoreCase(pago.getEstado())) {
            return pago;
        }

        Deuda deuda = deudaRepository.findByIdForUpdate(pago.getDeudaId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La deuda asociada no existe."
                        )
                );

        BigDecimal nuevoSaldo =
                deuda.getSaldoPendiente()
                        .subtract(pago.getMonto());

        if (nuevoSaldo.compareTo(BigDecimal.ZERO) <= 0) {

            deuda.setSaldoPendiente(BigDecimal.ZERO);
            deuda.setEstado("PAGADA");

        } else {

            deuda.setSaldoPendiente(nuevoSaldo);
            deuda.setEstado("PARCIAL");
        }

        deudaRepository.save(deuda);

        pago.setEstado("CONFIRMADO");

        if (pago.getFechaPago() == null) {
            pago.setFechaPago(LocalDateTime.now());
        }

        return pagoRepository.save(pago);
    }

    public Pago rechazarPago(String pagoId) {

        Pago pago = pagoRepository.findByIdForUpdate(pagoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Pago no encontrado."
                        )
                );

        if ("CONFIRMADO".equalsIgnoreCase(pago.getEstado())) {
            throw new IllegalArgumentException(
                    "Un pago confirmado no puede rechazarse."
            );
        }

        pago.setEstado("RECHAZADO");

        return pagoRepository.save(pago);
    }

    public Pago reembolsarPago(String pagoId) {

        Pago pago = pagoRepository.findByIdForUpdate(pagoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Pago no encontrado."
                        )
                );

        if (!"CONFIRMADO".equalsIgnoreCase(pago.getEstado())) {
            throw new IllegalArgumentException(
                    "Solo se pueden reembolsar pagos confirmados."
            );
        }

        Deuda deuda = deudaRepository.findByIdForUpdate(pago.getDeudaId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Deuda asociada no encontrada."
                        )
                );

        BigDecimal nuevoSaldo =
                deuda.getSaldoPendiente()
                        .add(pago.getMonto());

        deuda.setSaldoPendiente(nuevoSaldo);

        if (nuevoSaldo.compareTo(deuda.getMonto()) >= 0) {
            deuda.setSaldoPendiente(deuda.getMonto());
            deuda.setEstado("PENDIENTE");
        } else {
            deuda.setEstado("PARCIAL");
        }

        deudaRepository.save(deuda);

        pago.setEstado("REEMBOLSADO");

        return pagoRepository.save(pago);
    }

    public void eliminar(String id) {

        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Pago no encontrado."
                        )
                );

        if ("CONFIRMADO".equalsIgnoreCase(pago.getEstado())) {
            throw new IllegalArgumentException(
                    "No se puede eliminar un pago confirmado."
            );
        }

        pagoRepository.deleteById(id);
    }
}