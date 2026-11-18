package com.thales_mileto.matricula.service;

import com.thales_mileto.matricula.model.Matricula;
import com.thales_mileto.matricula.model.Vacante;
import com.thales_mileto.matricula.model.embedded.ReferenciaMigracion;
import com.thales_mileto.matricula.repository.MatriculaRepository;
import com.thales_mileto.matricula.service.VacanteService.Cupo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Toda operación que cambia una matrícula ajusta el cupo de su vacante en la MISMA
 * transacción, con la fila de la vacante bloqueada (SELECT ... FOR UPDATE):
 * o se guardan juntos la matrícula y el contador, o no se guarda ninguno.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final VacanteService vacanteService;
    private final PlatformTransactionManager transactionManager;

    @Transactional(readOnly = true)
    public List<Matricula> listar() {
        return matriculaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Matricula obtenerPorId(String id) {
        return matriculaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matrícula no encontrada"));
    }

    public Matricula crear(Matricula matricula, Principal principal) {
        matricula.setId(null);
        ReferenciaMigracion ref = matricula.getReferenciaMigracion();

        if (ref != null) {
            normalizarReferencia(ref);
            if (buscarPorReferencia(ref.getSistemaOrigen(), ref.getCodigoExterno()).isPresent()) {
                throw new IllegalStateException("La matrícula " + ref.getCodigoExterno() + " de "
                        + ref.getSistemaOrigen() + " ya fue migrada");
            }
        }

        // Una matrícula migrada conserva su estado y fecha originales si vienen informados.
        boolean migrada = ref != null;
        if (!migrada || matricula.getEstado() == null || matricula.getEstado().isBlank()) {
            matricula.setEstado("REGISTRADA");
        }
        if (!migrada || matricula.getFechaRegistro() == null) {
            matricula.setFechaRegistro(LocalDateTime.now());
        }
        matricula.setUsuarioRegistro(principal.getName());

        normalizarVacante(matricula);
        if (tieneVacante(matricula)) {
            Vacante vacante = bloquearVacante(matricula.getSeccionId(), matricula.getPeriodoAcademicoId());
            if (matricula.getSeccion() == null || matricula.getSeccion().isBlank()) {
                matricula.setSeccion(vacante.getNombreSeccion());
            }
            vacanteService.moverCupo(vacante, Cupo.NINGUNO, VacanteService.cupoSegunEstado(matricula.getEstado()));
        }

        return matriculaRepository.save(matricula);
    }

    /**
     * Importa un lote de matrículas desde otro sistema. Cada una debe traer
     * referenciaMigracion con sistemaOrigen y codigoExterno. Es idempotente:
     * las que ya fueron migradas se omiten, así el lote puede reenviarse sin duplicar.
     *
     * Cada matrícula se guarda en su propia transacción: un error en una fila
     * (p. ej. sección sin vacantes) no revierte las demás.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Map<String, Object> migrar(List<Matricula> matriculas, String loteMigracion, Principal principal) {
        List<Map<String, Object>> creadas = new ArrayList<>();
        List<Map<String, Object>> omitidas = new ArrayList<>();
        List<Map<String, Object>> errores = new ArrayList<>();

        TransactionTemplate porFila = new TransactionTemplate(transactionManager);
        porFila.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        for (int i = 0; i < matriculas.size(); i++) {
            Matricula m = matriculas.get(i);
            ReferenciaMigracion ref = m.getReferenciaMigracion();
            try {
                if (ref == null) {
                    throw new IllegalArgumentException("Falta referenciaMigracion");
                }
                if (loteMigracion != null && (ref.getLoteMigracion() == null || ref.getLoteMigracion().isBlank())) {
                    ref.setLoteMigracion(loteMigracion);
                }
                normalizarReferencia(ref);

                final int indice = i;
                Map<String, Object> resultado = porFila.execute(status -> {
                    Optional<Matricula> existente = buscarPorReferencia(ref.getSistemaOrigen(), ref.getCodigoExterno());
                    if (existente.isPresent()) {
                        return fila(indice, ref, existente.get().getId(), "Ya migrada");
                    }
                    Matricula guardada = crear(m, principal);
                    matriculaRepository.flush(); // detecta violaciones de UNIQUE/CHECK dentro de esta fila
                    return fila(indice, ref, guardada.getId(), null);
                });

                if (resultado != null && resultado.containsKey("detalle")) {
                    omitidas.add(resultado);
                } else {
                    creadas.add(resultado);
                }
            } catch (RuntimeException e) {
                errores.add(fila(i, ref, null, mensaje(e)));
            }
        }

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("total", matriculas.size());
        resumen.put("creadas", creadas);
        resumen.put("omitidas", omitidas);
        resumen.put("errores", errores);
        return resumen;
    }

    @Transactional(readOnly = true)
    public Optional<Matricula> buscarPorReferencia(String sistemaOrigen, String codigoExterno) {
        return matriculaRepository.buscarPorReferenciaMigracion(sistemaOrigen.trim(), codigoExterno.trim())
                .stream().findFirst();
    }

    @Transactional(readOnly = true)
    public List<Matricula> listarPorLote(String loteMigracion) {
        return matriculaRepository.buscarPorLoteMigracion(loteMigracion);
    }

    /**
     * Actualiza la matrícula y, si cambió su estado o su sección/periodo, mueve el cupo
     * correspondiente: p. ej. PENDIENTE → CONFIRMADA pasa de reservada a ocupada,
     * → ANULADA libera el cupo, y un cambio de sección libera en la anterior y toma en la nueva.
     */
    public Matricula actualizar(String id, Matricula datos) {
        Matricula matricula = bloquearMatricula(id);

        String seccionAnterior = matricula.getSeccionId();
        String periodoAnterior = matricula.getPeriodoAcademicoId();
        Cupo cupoAnterior = VacanteService.cupoSegunEstado(matricula.getEstado());

        normalizarVacante(datos);
        Cupo cupoNuevo = VacanteService.cupoSegunEstado(datos.getEstado());
        boolean mismaVacante = Objects.equals(seccionAnterior, datos.getSeccionId())
                && Objects.equals(periodoAnterior, datos.getPeriodoAcademicoId());

        if (mismaVacante) {
            if (seccionAnterior != null && cupoAnterior != cupoNuevo) {
                vacanteService.moverCupo(bloquearVacante(seccionAnterior, periodoAnterior), cupoAnterior, cupoNuevo);
            }
        } else {
            moverEntreVacantes(seccionAnterior, periodoAnterior, cupoAnterior,
                    datos.getSeccionId(), datos.getPeriodoAcademicoId(), cupoNuevo);
        }

        matricula.setEstudianteId(datos.getEstudianteId());
        matricula.setNombreEstudiante(datos.getNombreEstudiante());
        matricula.setGrado(datos.getGrado());
        matricula.setSeccion(datos.getSeccion());
        matricula.setPeriodo(datos.getPeriodo());
        matricula.setEstado(datos.getEstado());
        matricula.setSeccionId(datos.getSeccionId());
        matricula.setPeriodoAcademicoId(datos.getPeriodoAcademicoId());
        // referenciaMigracion no se modifica: es el rastro del registro original.

        return matriculaRepository.save(matricula);
    }

    /** Elimina la matrícula y libera su cupo en la misma transacción. */
    public void eliminar(String id) {
        Matricula matricula = bloquearMatricula(id);

        if (tieneVacante(matricula)) {
            vacanteService.bloquear(matricula.getSeccionId(), matricula.getPeriodoAcademicoId())
                    .ifPresent(v -> vacanteService.moverCupo(v,
                            VacanteService.cupoSegunEstado(matricula.getEstado()), Cupo.NINGUNO));
        }

        matriculaRepository.delete(matricula);
    }

    // =========================================================
    // Auxiliares de vacantes
    // =========================================================

    /**
     * Libera el cupo en la vacante anterior y lo toma en la nueva. Ambas filas se bloquean
     * siempre en el mismo orden (por clave) para que dos traslados cruzados no se bloqueen
     * mutuamente (deadlock).
     */
    private void moverEntreVacantes(String seccionAnterior, String periodoAnterior, Cupo cupoAnterior,
                                    String seccionNueva, String periodoNuevo, Cupo cupoNuevo) {
        String claveAnterior = seccionAnterior == null ? null : seccionAnterior + "|" + periodoAnterior;
        String claveNueva = seccionNueva == null ? null : seccionNueva + "|" + periodoNuevo;

        Vacante anterior = null;
        Vacante nueva = null;
        boolean anteriorPrimero = claveNueva == null
                || (claveAnterior != null && claveAnterior.compareTo(claveNueva) < 0);

        if (anteriorPrimero) {
            anterior = claveAnterior == null ? null : vacanteService.bloquear(seccionAnterior, periodoAnterior).orElse(null);
            nueva = claveNueva == null ? null : bloquearVacante(seccionNueva, periodoNuevo);
        } else {
            nueva = bloquearVacante(seccionNueva, periodoNuevo);
            anterior = claveAnterior == null ? null : vacanteService.bloquear(seccionAnterior, periodoAnterior).orElse(null);
        }

        // Si la vacante anterior ya no existe no hay cupo que liberar.
        if (anterior != null) {
            vacanteService.moverCupo(anterior, cupoAnterior, Cupo.NINGUNO);
        }
        if (nueva != null) {
            vacanteService.moverCupo(nueva, Cupo.NINGUNO, cupoNuevo);
        }
    }

    private Vacante bloquearVacante(String seccionId, String periodoAcademicoId) {
        return vacanteService.bloquear(seccionId, periodoAcademicoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe una vacante para la sección "
                        + seccionId + " en el periodo " + periodoAcademicoId));
    }

    private Matricula bloquearMatricula(String id) {
        return matriculaRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RuntimeException("Matrícula no encontrada"));
    }

    /** seccionId y periodoAcademicoId van juntos: ambos o ninguno. */
    private void normalizarVacante(Matricula m) {
        m.setSeccionId(vacioANull(m.getSeccionId()));
        m.setPeriodoAcademicoId(vacioANull(m.getPeriodoAcademicoId()));
        if ((m.getSeccionId() == null) != (m.getPeriodoAcademicoId() == null)) {
            throw new IllegalArgumentException("seccionId y periodoAcademicoId deben enviarse juntos");
        }
    }

    private static boolean tieneVacante(Matricula m) {
        return m.getSeccionId() != null && m.getPeriodoAcademicoId() != null;
    }

    // =========================================================
    // Auxiliares de migración
    // =========================================================

    private void normalizarReferencia(ReferenciaMigracion ref) {
        if (ref.getSistemaOrigen() == null || ref.getSistemaOrigen().isBlank()) {
            throw new IllegalArgumentException("referenciaMigracion.sistemaOrigen es obligatorio");
        }
        if (ref.getCodigoExterno() == null || ref.getCodigoExterno().isBlank()) {
            throw new IllegalArgumentException("referenciaMigracion.codigoExterno es obligatorio");
        }
        ref.setSistemaOrigen(ref.getSistemaOrigen().trim().toUpperCase());
        ref.setCodigoExterno(ref.getCodigoExterno().trim());
        if (ref.getFechaMigracion() == null) {
            ref.setFechaMigracion(LocalDateTime.now());
        }
    }

    private Map<String, Object> fila(int indice, ReferenciaMigracion ref, String matriculaId, String detalle) {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("indice", indice);
        fila.put("sistemaOrigen", ref == null ? null : ref.getSistemaOrigen());
        fila.put("codigoExterno", ref == null ? null : ref.getCodigoExterno());
        if (matriculaId != null) fila.put("matriculaId", matriculaId);
        if (detalle != null) fila.put("detalle", detalle);
        return fila;
    }

    private static String mensaje(RuntimeException e) {
        Throwable causa = e;
        while (causa.getCause() != null && causa.getCause() != causa) {
            causa = causa.getCause();
        }
        return causa == e ? e.getMessage() : e.getMessage() + " (" + causa.getMessage() + ")";
    }

    private static String vacioANull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
