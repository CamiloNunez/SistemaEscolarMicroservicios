package com.thales_mileto.matricula.service;

import com.thales_mileto.matricula.model.Vacante;
import com.thales_mileto.matricula.repository.VacanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class VacanteService {

    /** Cupo que consume una matrícula en su vacante, según su estado. */
    public enum Cupo { NINGUNO, RESERVADO, OCUPADO }

    /** Estados de matrícula que no consumen cupo. */
    private static final Set<String> ESTADOS_SIN_CUPO =
            Set.of("ANULADA", "CANCELADA", "RECHAZADA", "RETIRADA");

    private final VacanteRepository vacanteRepository;

    @Transactional(readOnly = true)
    public List<Vacante> listar() {
        return vacanteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Vacante obtenerPorId(String id) {
        return vacanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vacante no encontrada"));
    }

    public Vacante crear(Vacante vacante, Principal principal) {
        vacante.setId(null);

        if (vacanteRepository.existsBySeccionIdAndPeriodoAcademicoId(
                vacante.getSeccionId(),
                vacante.getPeriodoAcademicoId()
        )) {
            throw new RuntimeException("Ya existe una vacante para esa sección y periodo académico");
        }

        if (vacante.getVacantesOcupadas() == null) {
            vacante.setVacantesOcupadas(0);
        }

        if (vacante.getVacantesReservadas() == null) {
            vacante.setVacantesReservadas(0);
        }

        validarTotal(vacante.getTotalVacantes(), usados(vacante));

        vacante.setEstado(Vacante.DISPONIBLE);
        vacante.recalcularEstado();
        vacante.setUsuarioRegistro(principal.getName());
        vacante.setFechaRegistro(LocalDateTime.now());
        vacante.setFechaActualizacion(LocalDateTime.now());

        return vacanteRepository.save(vacante);
    }

    /**
     * Actualiza los datos de la vacante. vacantesOcupadas y vacantesReservadas NO se
     * modifican aquí: los mantiene MatriculaService al crear, confirmar, anular o
     * eliminar matrículas. Se ignoran si vienen en el cuerpo.
     */
    public Vacante actualizar(String id, Vacante datos) {
        Vacante vacante = bloquearPorId(id);
        int usados = usados(vacante);

        boolean cambiaSeccion = !Objects.equals(vacante.getSeccionId(), datos.getSeccionId())
                || !Objects.equals(vacante.getPeriodoAcademicoId(), datos.getPeriodoAcademicoId());
        if (cambiaSeccion) {
            if (usados > 0) {
                throw new IllegalStateException("No se puede cambiar la sección o el periodo de una vacante con "
                        + usados + " cupo(s) en uso");
            }
            vacanteRepository.findBySeccionIdAndPeriodoAcademicoId(datos.getSeccionId(), datos.getPeriodoAcademicoId())
                    .filter(otra -> !otra.getId().equals(id))
                    .ifPresent(otra -> {
                        throw new IllegalStateException("Ya existe una vacante para esa sección y periodo académico");
                    });
        }

        validarTotal(datos.getTotalVacantes(), usados);

        vacante.setSeccionId(datos.getSeccionId());
        vacante.setPeriodoAcademicoId(datos.getPeriodoAcademicoId());
        vacante.setGradoId(datos.getGradoId());
        vacante.setNombreSeccion(datos.getNombreSeccion());
        vacante.setTotalVacantes(datos.getTotalVacantes());
        // CERRADA es una decisión explícita; cualquier otro valor se recalcula según los cupos.
        vacante.setEstado(Vacante.CERRADA.equals(datos.getEstado()) ? Vacante.CERRADA : Vacante.DISPONIBLE);
        vacante.recalcularEstado();
        vacante.setFechaActualizacion(LocalDateTime.now());

        return vacanteRepository.save(vacante);
    }

    public void eliminar(String id) {
        Vacante vacante = bloquearPorId(id);
        int usados = usados(vacante);
        if (usados > 0) {
            throw new IllegalStateException("No se puede eliminar una vacante con " + usados
                    + " cupo(s) en uso. Anule o elimine primero esas matrículas");
        }
        vacanteRepository.delete(vacante);
    }

    // =========================================================
    // Control de cupos (usado por MatriculaService)
    // =========================================================

    public static Cupo cupoSegunEstado(String estadoMatricula) {
        if ("CONFIRMADA".equals(estadoMatricula)) {
            return Cupo.OCUPADO;
        }
        if (estadoMatricula != null && ESTADOS_SIN_CUPO.contains(estadoMatricula)) {
            return Cupo.NINGUNO;
        }
        // REGISTRADA, PENDIENTE o cualquier otro estado en proceso retiene el cupo.
        return Cupo.RESERVADO;
    }

    /**
     * Bloquea (SELECT ... FOR UPDATE) la vacante de la sección y periodo. Debe llamarse
     * dentro de la transacción que modifica la matrícula: el bloqueo dura hasta el commit.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<Vacante> bloquear(String seccionId, String periodoAcademicoId) {
        return vacanteRepository.findBySeccionIdAndPeriodoAcademicoIdForUpdate(seccionId, periodoAcademicoId);
    }

    /**
     * Mueve el cupo de una matrícula de {@code antes} a {@code despues} sobre una vacante
     * ya bloqueada con {@link #bloquear}. Solo exige cupo libre cuando la matrícula pasa
     * de no tener cupo a tenerlo; confirmar una reserva no consume un cupo nuevo.
     *
     * @throws IllegalStateException si la vacante está cerrada o llena (HTTP 409).
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void moverCupo(Vacante vacante, Cupo antes, Cupo despues) {
        if (antes == despues) {
            return;
        }

        if (antes == Cupo.NINGUNO) {
            if (Vacante.CERRADA.equals(vacante.getEstado())) {
                throw new IllegalStateException("La vacante de " + vacante.getNombreSeccion() + " está cerrada");
            }
            if (vacante.getVacantesDisponibles() <= 0) {
                throw new IllegalStateException("No hay vacantes disponibles en " + vacante.getNombreSeccion()
                        + " (" + vacante.getTotalVacantes() + " en total)");
            }
        }

        ajustar(vacante, antes, -1);
        ajustar(vacante, despues, +1);

        vacante.recalcularEstado();
        vacante.setFechaActualizacion(LocalDateTime.now());
        vacanteRepository.save(vacante);
    }

    private void ajustar(Vacante vacante, Cupo cupo, int delta) {
        switch (cupo) {
            case RESERVADO -> vacante.setVacantesReservadas(verificar(vacante, vacante.getVacantesReservadas() + delta));
            case OCUPADO -> vacante.setVacantesOcupadas(verificar(vacante, vacante.getVacantesOcupadas() + delta));
            case NINGUNO -> { }
        }
    }

    private int verificar(Vacante vacante, int valor) {
        if (valor < 0) {
            throw new IllegalStateException("Los contadores de la vacante " + vacante.getId()
                    + " están desincronizados con sus matrículas (quedarían negativos)");
        }
        return valor;
    }

    private Vacante bloquearPorId(String id) {
        return vacanteRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RuntimeException("Vacante no encontrada"));
    }

    private static int usados(Vacante v) {
        return (v.getVacantesOcupadas() == null ? 0 : v.getVacantesOcupadas())
                + (v.getVacantesReservadas() == null ? 0 : v.getVacantesReservadas());
    }

    private static void validarTotal(Integer total, int usados) {
        if (total == null || total < 0) {
            throw new IllegalArgumentException("totalVacantes es obligatorio y no puede ser negativo");
        }
        if (total < usados) {
            throw new IllegalStateException("totalVacantes (" + total + ") no puede ser menor que los cupos en uso ("
                    + usados + ")");
        }
    }
}
