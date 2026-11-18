package com.thales_mileto.matricula.repository;

import com.thales_mileto.matricula.model.Vacante;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VacanteRepository extends JpaRepository<Vacante, String> {

    /**
     * Obtiene la vacante bloqueando la fila (SELECT ... FOR UPDATE) hasta el fin
     * de la transacción. Dos matrículas simultáneas sobre la misma sección se
     * atienden una detrás de otra, así nunca se asigna más cupos de los que hay.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Vacante v WHERE v.id = :id")
    Optional<Vacante> findByIdForUpdate(@Param("id") String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Vacante v WHERE v.seccionId = :seccionId AND v.periodoAcademicoId = :periodoAcademicoId")
    Optional<Vacante> findBySeccionIdAndPeriodoAcademicoIdForUpdate(@Param("seccionId") String seccionId,
                                                                     @Param("periodoAcademicoId") String periodoAcademicoId);

    List<Vacante> findBySeccionId(String seccionId);

    List<Vacante> findByPeriodoAcademicoId(String periodoAcademicoId);

    List<Vacante> findByGradoId(String gradoId);

    Optional<Vacante> findBySeccionIdAndPeriodoAcademicoId(String seccionId, String periodoAcademicoId);

    boolean existsBySeccionIdAndPeriodoAcademicoId(String seccionId, String periodoAcademicoId);

    List<Vacante> findByEstado(String estado);
}
