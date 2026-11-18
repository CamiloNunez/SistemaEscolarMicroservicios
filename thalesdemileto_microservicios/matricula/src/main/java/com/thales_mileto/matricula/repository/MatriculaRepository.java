package com.thales_mileto.matricula.repository;

import com.thales_mileto.matricula.model.Matricula;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, String> {

    /**
     * Obtiene la matrícula bloqueando la fila (SELECT ... FOR UPDATE) hasta el fin
     * de la transacción, para que dos cambios simultáneos de la misma matrícula
     * no liberen o consuman su cupo dos veces.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Matricula m WHERE m.id = :id")
    Optional<Matricula> findByIdForUpdate(@Param("id") String id);

    List<Matricula> findByEstudianteId(String estudianteId);

    List<Matricula> findByPeriodo(String periodo);

    List<Matricula> findByEstado(String estado);

    List<Matricula> findByGrado(String grado);

    List<Matricula> findBySeccion(String seccion);

    boolean existsByEstudianteIdAndPeriodo(String estudianteId, String periodo);

    @Query("SELECT m FROM Matricula m WHERE m.referenciaMigracion.sistemaOrigen = :sistemaOrigen "
            + "AND m.referenciaMigracion.codigoExterno = :codigoExterno")
    List<Matricula> buscarPorReferenciaMigracion(@Param("sistemaOrigen") String sistemaOrigen,
                                                 @Param("codigoExterno") String codigoExterno);

    @Query("SELECT m FROM Matricula m WHERE m.referenciaMigracion.loteMigracion = :loteMigracion")
    List<Matricula> buscarPorLoteMigracion(@Param("loteMigracion") String loteMigracion);
}
