package com.thales_mileto.pagos.repository;

import com.thales_mileto.pagos.model.Deuda;
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
public interface DeudaRepository
        extends JpaRepository<Deuda, String> {

    /**
     * Obtiene la deuda bloqueando la fila (SELECT ... FOR UPDATE) hasta el fin
     * de la transacción, evitando que dos pagos concurrentes descuenten el
     * mismo saldo pendiente.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Deuda d WHERE d.id = :id")
    Optional<Deuda> findByIdForUpdate(@Param("id") String id);

    List<Deuda> findByEstudianteId(String estudianteId);

    List<Deuda> findByMatriculaId(String matriculaId);

    List<Deuda> findByConcepto(String concepto);

    List<Deuda> findByEstado(String estado);

    List<Deuda> findByEstudianteIdAndEstado(
            String estudianteId,
            String estado
    );

    List<Deuda> findByMatriculaIdAndEstado(
            String matriculaId,
            String estado
    );

    List<Deuda> findByEstudianteIdAndConcepto(
            String estudianteId,
            String concepto
    );

    List<Deuda> findByEstudianteIdAndConceptoAndEstado(
            String estudianteId,
            String concepto,
            String estado
    );

    List<Deuda> findByFechaVencimientoBefore(
            LocalDateTime fecha
    );

    List<Deuda> findByEstadoAndFechaVencimientoBefore(
            String estado,
            LocalDateTime fecha
    );

    boolean existsByEstudianteIdAndMatriculaIdAndConceptoAndEstado(
            String estudianteId,
            String matriculaId,
            String concepto,
            String estado
    );
}