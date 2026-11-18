package com.thales_mileto.academico.service;

import com.thales_mileto.academico.model.Prerequisito;
import com.thales_mileto.academico.repository.PrerequisitoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PrerequisitoService {

    private final PrerequisitoRepository prerequisitoRepository;

    public PrerequisitoService(
            PrerequisitoRepository prerequisitoRepository) {
        this.prerequisitoRepository = prerequisitoRepository;
    }

    public List<Prerequisito> listarTodos() {
        return prerequisitoRepository.findAll();
    }

    public Prerequisito buscarPorId(String id) {
        return prerequisitoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Prerrequisito no encontrado"
                        )
                );
    }

    public List<Prerequisito> buscarPorCurso(String cursoId) {
        return prerequisitoRepository.findByCursoId(cursoId);
    }

    public List<Prerequisito> buscarCursosQueRequieren(
            String cursoPrerequisitoId) {

        return prerequisitoRepository
                .findByCursoPrerequisitoId(
                        cursoPrerequisitoId
                );
    }

    public List<Prerequisito> buscarActivosPorCurso(
            String cursoId) {

        return prerequisitoRepository
                .findByCursoIdAndEstado(
                        cursoId,
                        "ACTIVO"
                );
    }

    public List<Prerequisito> buscarPorCursoYEstado(
            String cursoId,
            String estado) {

        return prerequisitoRepository
                .findByCursoIdAndEstado(
                        cursoId,
                        estado
                );
    }

    public List<Prerequisito> buscarPorTipo(String tipo) {
        return prerequisitoRepository.findByTipo(tipo);
    }

    public List<Prerequisito> buscarPorEstado(String estado) {
        return prerequisitoRepository.findByEstado(estado);
    }

    public Optional<Prerequisito> buscarRelacion(
            String cursoId,
            String cursoPrerequisitoId) {

        return prerequisitoRepository
                .findByCursoIdAndCursoPrerequisitoId(
                        cursoId,
                        cursoPrerequisitoId
                );
    }

    public Prerequisito crear(Prerequisito prerequisito) {

        validarDatos(prerequisito);

        if (prerequisito.getCursoId()
                .equals(prerequisito.getCursoPrerequisitoId())) {

            throw new IllegalArgumentException(
                    "Un curso no puede ser prerrequisito de sí mismo"
            );
        }

        boolean existe = prerequisitoRepository
                .existsByCursoIdAndCursoPrerequisitoId(
                        prerequisito.getCursoId(),
                        prerequisito.getCursoPrerequisitoId()
                );

        if (existe) {
            throw new IllegalArgumentException(
                    "El prerrequisito ya se encuentra registrado para este curso"
            );
        }

        prerequisito.setId(null);

        if (prerequisito.getTipo() == null ||
                prerequisito.getTipo().isBlank()) {
            prerequisito.setTipo("OBLIGATORIO");
        }

        if (prerequisito.getEstado() == null ||
                prerequisito.getEstado().isBlank()) {
            prerequisito.setEstado("ACTIVO");
        }

        return prerequisitoRepository.save(prerequisito);
    }

    public Prerequisito actualizar(
            String id,
            Prerequisito datos) {

        validarDatos(datos);

        if (datos.getCursoId()
                .equals(datos.getCursoPrerequisitoId())) {

            throw new IllegalArgumentException(
                    "Un curso no puede ser prerrequisito de sí mismo"
            );
        }

        Prerequisito actual = buscarPorId(id);

        Optional<Prerequisito> existente =
                prerequisitoRepository
                        .findByCursoIdAndCursoPrerequisitoId(
                                datos.getCursoId(),
                                datos.getCursoPrerequisitoId()
                        );

        if (existente.isPresent()
                && !existente.get().getId().equals(id)) {

            throw new IllegalArgumentException(
                    "La relación de prerrequisito ya existe"
            );
        }

        actual.setCursoId(datos.getCursoId());
        actual.setCursoPrerequisitoId(
                datos.getCursoPrerequisitoId()
        );
        actual.setTipo(datos.getTipo());
        actual.setEstado(datos.getEstado());

        return prerequisitoRepository.save(actual);
    }

    public Prerequisito cambiarEstado(
            String id,
            String estado) {

        Prerequisito prerequisito = buscarPorId(id);

        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException(
                    "El estado es obligatorio"
            );
        }

        prerequisito.setEstado(estado);

        return prerequisitoRepository.save(prerequisito);
    }

    public void eliminar(String id) {

        if (!prerequisitoRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Prerrequisito no encontrado"
            );
        }

        prerequisitoRepository.deleteById(id);
    }

    private void validarDatos(Prerequisito prerequisito) {

        if (prerequisito == null) {
            throw new IllegalArgumentException(
                    "El prerrequisito es obligatorio"
            );
        }

        if (prerequisito.getCursoId() == null ||
                prerequisito.getCursoId().isBlank()) {
            throw new IllegalArgumentException(
                    "El curso es obligatorio"
            );
        }

        if (prerequisito.getCursoPrerequisitoId() == null ||
                prerequisito.getCursoPrerequisitoId().isBlank()) {
            throw new IllegalArgumentException(
                    "El curso prerrequisito es obligatorio"
            );
        }
    }
}