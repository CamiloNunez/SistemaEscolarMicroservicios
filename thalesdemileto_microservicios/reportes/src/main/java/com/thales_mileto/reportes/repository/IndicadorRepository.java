package com.thales_mileto.reportes.repository;

import com.thales_mileto.reportes.model.Indicador;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IndicadorRepository
        extends MongoRepository<Indicador, String> {

    List<Indicador> findByCodigo(String codigo);

    List<Indicador> findByTipo(String tipo);

    List<Indicador> findByPeriodo(String periodo);

    List<Indicador> findByCodigoAndPeriodo(
            String codigo,
            String periodo
    );

    List<Indicador> findByTipoAndPeriodo(
            String tipo,
            String periodo
    );

    List<Indicador> findByFechaCalculoBetween(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<Indicador> findByCodigoAndFechaCalculoBetween(
            String codigo,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    Optional<Indicador> findFirstByCodigoOrderByFechaCalculoDesc(
            String codigo
    );

    Optional<Indicador> findFirstByCodigoAndPeriodoOrderByFechaCalculoDesc(
            String codigo,
            String periodo
    );
}