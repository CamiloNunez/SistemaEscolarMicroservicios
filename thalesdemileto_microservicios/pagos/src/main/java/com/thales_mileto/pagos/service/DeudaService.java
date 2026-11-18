package com.thales_mileto.pagos.service;

import com.thales_mileto.pagos.model.Deuda;
import com.thales_mileto.pagos.repository.DeudaRepository;
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
public class DeudaService {

    private final DeudaRepository deudaRepository;

    public List<Deuda> listarTodas() {
        return deudaRepository.findAll();
    }

    public Optional<Deuda> buscarPorId(String id) {
        return deudaRepository.findById(id);
    }

    public List<Deuda> buscarPorEstudiante(String estudianteId) {
        return deudaRepository.findByEstudianteId(estudianteId);
    }

    public List<Deuda> buscarPorMatricula(String matriculaId) {
        return deudaRepository.findByMatriculaId(matriculaId);
    }

    public List<Deuda> buscarPorConcepto(String concepto) {
        return deudaRepository.findByConcepto(concepto);
    }

    public List<Deuda> buscarPorEstado(String estado) {
        return deudaRepository.findByEstado(estado);
    }

    public List<Deuda> buscarPorEstudianteYEstado(
            String estudianteId,
            String estado
    ) {
        return deudaRepository.findByEstudianteIdAndEstado(
                estudianteId,
                estado
        );
    }

    public List<Deuda> buscarPorMatriculaYEstado(
            String matriculaId,
            String estado
    ) {
        return deudaRepository.findByMatriculaIdAndEstado(
                matriculaId,
                estado
        );
    }

    public List<Deuda> buscarPorEstudianteYConcepto(
            String estudianteId,
            String concepto
    ) {
        return deudaRepository.findByEstudianteIdAndConcepto(
                estudianteId,
                concepto
        );
    }

    public List<Deuda> buscarPorEstudianteConceptoYEstado(
            String estudianteId,
            String concepto,
            String estado
    ) {
        return deudaRepository
                .findByEstudianteIdAndConceptoAndEstado(
                        estudianteId,
                        concepto,
                        estado
                );
    }

    public List<Deuda> listarVencidas() {
        return deudaRepository.findByFechaVencimientoBefore(
                LocalDateTime.now()
        );
    }

    public List<Deuda> listarPendientesVencidas() {
        return deudaRepository.findByEstadoAndFechaVencimientoBefore(
                "PENDIENTE",
                LocalDateTime.now()
        );
    }

    public Deuda guardar(Deuda deuda) {

        if (deuda.getEstudianteId() == null ||
                deuda.getEstudianteId().isBlank()) {
            throw new IllegalArgumentException(
                    "El estudianteId es obligatorio."
            );
        }

        if (deuda.getMonto() == null ||
                deuda.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser mayor a cero."
            );
        }

        deuda.setFechaRegistro(LocalDateTime.now());

        if (deuda.getFechaEmision() == null) {
            deuda.setFechaEmision(LocalDateTime.now());
        }

        if (deuda.getSaldoPendiente() == null) {
            deuda.setSaldoPendiente(deuda.getMonto());
        }

        if (deuda.getMoneda() == null ||
                deuda.getMoneda().isBlank()) {
            deuda.setMoneda("PEN");
        }

        if (deuda.getEstado() == null ||
                deuda.getEstado().isBlank()) {
            deuda.setEstado("PENDIENTE");
        }

        return deudaRepository.save(deuda);
    }

    public Deuda actualizar(
            String id,
            Deuda datos
    ) {

        Deuda deuda = deudaRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Deuda no encontrada."
                        )
                );

        deuda.setEstudianteId(datos.getEstudianteId());
        deuda.setMatriculaId(datos.getMatriculaId());
        deuda.setConcepto(datos.getConcepto());
        deuda.setMonto(datos.getMonto());
        deuda.setSaldoPendiente(datos.getSaldoPendiente());
        deuda.setMoneda(datos.getMoneda());
        deuda.setFechaEmision(datos.getFechaEmision());
        deuda.setFechaVencimiento(datos.getFechaVencimiento());
        deuda.setEstado(datos.getEstado());

        return deudaRepository.save(deuda);
    }

    public Deuda actualizarSaldo(
            String deudaId,
            BigDecimal montoPagado
    ) {

        Deuda deuda = deudaRepository.findById(deudaId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Deuda no encontrada."
                        )
                );

        if (montoPagado == null ||
                montoPagado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El monto pagado debe ser mayor a cero."
            );
        }

        BigDecimal saldoActual = deuda.getSaldoPendiente();

        BigDecimal nuevoSaldo =
                saldoActual.subtract(montoPagado);

        if (nuevoSaldo.compareTo(BigDecimal.ZERO) <= 0) {
            nuevoSaldo = BigDecimal.ZERO;
            deuda.setEstado("PAGADA");
        } else {
            deuda.setEstado("PARCIAL");
        }

        deuda.setSaldoPendiente(nuevoSaldo);

        return deudaRepository.save(deuda);
    }

    public Deuda anular(String id) {

        Deuda deuda = deudaRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Deuda no encontrada."
                        )
                );

        deuda.setEstado("ANULADA");

        return deudaRepository.save(deuda);
    }

    public void eliminar(String id) {

        if (!deudaRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Deuda no encontrada."
            );
        }

        deudaRepository.deleteById(id);
    }
}