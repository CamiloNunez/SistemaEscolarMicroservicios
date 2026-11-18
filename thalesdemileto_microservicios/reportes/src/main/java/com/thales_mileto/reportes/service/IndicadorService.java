package com.thales_mileto.reportes.service;

import com.thales_mileto.reportes.model.Indicador;
import com.thales_mileto.reportes.repository.IndicadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IndicadorService {

    private final IndicadorRepository repository;

    // =========================================================
    // LISTAR
    // =========================================================

    public List<Indicador> listarTodos() {
        return repository.findAll();
    }

    public Optional<Indicador> buscarPorId(String id) {
        return repository.findById(id);
    }

    // =========================================================
    // BÚSQUEDAS
    // =========================================================

    public List<Indicador> buscarPorCodigo(String codigo) {
        return repository.findByCodigo(codigo);
    }

    public List<Indicador> buscarPorTipo(String tipo) {
        return repository.findByTipo(tipo);
    }

    public List<Indicador> buscarPorPeriodo(String periodo) {
        return repository.findByPeriodo(periodo);
    }

    public List<Indicador> buscarPorCodigoYPeriodo(
            String codigo,
            String periodo
    ) {
        return repository.findByCodigoAndPeriodo(
                codigo,
                periodo
        );
    }

    public List<Indicador> buscarPorTipoYPeriodo(
            String tipo,
            String periodo
    ) {
        return repository.findByTipoAndPeriodo(
                tipo,
                periodo
        );
    }

    public List<Indicador> buscarPorFechas(
            LocalDateTime inicio,
            LocalDateTime fin
    ) {

        validarRangoFechas(inicio, fin);

        return repository.findByFechaCalculoBetween(
                inicio,
                fin
        );
    }

    public List<Indicador> buscarPorCodigoYFechas(
            String codigo,
            LocalDateTime inicio,
            LocalDateTime fin
    ) {

        validarRangoFechas(inicio, fin);

        return repository.findByCodigoAndFechaCalculoBetween(
                codigo,
                inicio,
                fin
        );
    }

    // =========================================================
    // ÚLTIMO INDICADOR
    // =========================================================

    public Optional<Indicador> obtenerUltimoPorCodigo(
            String codigo
    ) {
        return repository.findFirstByCodigoOrderByFechaCalculoDesc(
                codigo
        );
    }

    public Optional<Indicador> obtenerUltimoPorCodigoYPeriodo(
            String codigo,
            String periodo
    ) {
        return repository
                .findFirstByCodigoAndPeriodoOrderByFechaCalculoDesc(
                        codigo,
                        periodo
                );
    }

    // =========================================================
    // GUARDAR
    // =========================================================

    public Indicador guardar(Indicador indicador) {

        if (indicador == null) {
            throw new IllegalArgumentException(
                    "El indicador es obligatorio"
            );
        }

        validarIndicador(indicador);

        if (indicador.getFechaCalculo() == null) {
            indicador.setFechaCalculo(LocalDateTime.now());
        }

        indicador.setCodigo(
                indicador.getCodigo().trim().toUpperCase()
        );

        indicador.setTipo(
                indicador.getTipo().trim().toUpperCase()
        );

        return repository.save(indicador);
    }

    // =========================================================
    // REGISTRAR INDICADOR
    // =========================================================

    public Indicador registrarIndicador(
            String codigo,
            String nombre,
            String tipo,
            Double valor,
            String periodo
    ) {

        Indicador indicador = new Indicador();

        indicador.setCodigo(codigo);
        indicador.setNombre(nombre);
        indicador.setTipo(tipo);
        indicador.setValor(valor);
        indicador.setPeriodo(periodo);
        indicador.setFechaCalculo(LocalDateTime.now());

        return guardar(indicador);
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================

    public Indicador actualizar(
            String id,
            Indicador datos
    ) {

        Indicador existente = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Indicador no encontrado"
                        )
                );

        if (datos.getCodigo() != null) {
            existente.setCodigo(
                    datos.getCodigo().trim().toUpperCase()
            );
        }

        if (datos.getNombre() != null) {
            existente.setNombre(datos.getNombre());
        }

        if (datos.getTipo() != null) {
            existente.setTipo(
                    datos.getTipo().trim().toUpperCase()
            );
        }

        if (datos.getValor() != null) {
            existente.setValor(datos.getValor());
        }

        if (datos.getPeriodo() != null) {
            existente.setPeriodo(datos.getPeriodo());
        }

        if (datos.getFechaCalculo() != null) {
            existente.setFechaCalculo(
                    datos.getFechaCalculo()
            );
        }

        validarIndicador(existente);

        return repository.save(existente);
    }

    // =========================================================
    // ELIMINAR
    // =========================================================

    public void eliminar(String id) {

        if (!repository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Indicador no encontrado"
            );
        }

        repository.deleteById(id);
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarIndicador(Indicador indicador) {

        if (esVacio(indicador.getCodigo())) {
            throw new IllegalArgumentException(
                    "El código del indicador es obligatorio"
            );
        }

        if (esVacio(indicador.getNombre())) {
            throw new IllegalArgumentException(
                    "El nombre del indicador es obligatorio"
            );
        }

        if (esVacio(indicador.getTipo())) {
            throw new IllegalArgumentException(
                    "El tipo del indicador es obligatorio"
            );
        }

        if (indicador.getValor() == null) {
            throw new IllegalArgumentException(
                    "El valor del indicador es obligatorio"
            );
        }

        if (esVacio(indicador.getPeriodo())) {
            throw new IllegalArgumentException(
                    "El periodo del indicador es obligatorio"
            );
        }
    }

    private void validarRangoFechas(
            LocalDateTime inicio,
            LocalDateTime fin
    ) {

        if (inicio == null || fin == null) {
            throw new IllegalArgumentException(
                    "La fecha de inicio y fin son obligatorias"
            );
        }

        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException(
                    "La fecha de inicio no puede ser posterior a la fecha final"
            );
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}