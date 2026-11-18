package com.thales_mileto.pagos.repository;

import com.thales_mileto.pagos.model.Pago;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository
        extends JpaRepository<Pago, String> {

    /** Bloquea la fila del pago para evitar confirmaciones/reembolsos concurrentes. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pago p WHERE p.id = :id")
    Optional<Pago> findByIdForUpdate(@Param("id") String id);

    List<Pago> findByDeudaId(String deudaId);

    List<Pago> findByEstudianteId(String estudianteId);

    List<Pago> findByMatriculaId(String matriculaId);

    List<Pago> findByEstado(String estado);

    List<Pago> findByMetodoPago(String metodoPago);

    Optional<Pago> findByReferenciaExterna(
            String referenciaExterna
    );

    List<Pago> findByEstudianteIdAndEstado(
            String estudianteId,
            String estado
    );

    List<Pago> findByMatriculaIdAndEstado(
            String matriculaId,
            String estado
    );

    List<Pago> findByDeudaIdAndEstado(
            String deudaId,
            String estado
    );

    List<Pago> findByMetodoPagoAndEstado(
            String metodoPago,
            String estado
    );

    List<Pago> findByFechaPagoBetween(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<Pago> findByEstudianteIdAndFechaPagoBetween(
            String estudianteId,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    boolean existsByReferenciaExterna(String referenciaExterna);
}