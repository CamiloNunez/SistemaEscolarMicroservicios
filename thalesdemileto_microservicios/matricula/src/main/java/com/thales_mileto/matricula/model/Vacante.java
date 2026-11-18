package com.thales_mileto.matricula.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;

/**
 * Cupos de una sección en un periodo. Los contadores vacantesOcupadas y
 * vacantesReservadas los mantiene MatriculaService dentro de la misma transacción
 * que crea/actualiza/elimina la matrícula, bloqueando la fila (SELECT ... FOR UPDATE).
 */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "vacantes", uniqueConstraints =
        @UniqueConstraint(name = "uk_vacante_seccion_periodo",
                columnNames = {"seccion_id", "periodo_academico_id"}))
// Última línea de defensa: aunque un bug saltara la validación del servicio,
// PostgreSQL rechaza cualquier escritura que deje cupos negativos o sobrevendidos.
@Check(name = "ck_vacante_cupos", constraints =
        "vacantes_ocupadas >= 0 AND vacantes_reservadas >= 0 AND total_vacantes >= 0 "
                + "AND vacantes_ocupadas + vacantes_reservadas <= total_vacantes")
public class Vacante {

    public static final String DISPONIBLE = "DISPONIBLE";
    public static final String LLENA = "LLENA";
    public static final String CERRADA = "CERRADA";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @Column(name = "seccion_id", length = 36, nullable = false)
    private String seccionId;

    @Column(name = "periodo_academico_id", length = 36, nullable = false)
    private String periodoAcademicoId;

    // Desnormalizado para consultas de disponibilidad sin joins
    private String gradoId;
    private String nombreSeccion;       // "1° A"

    @Column(nullable = false)
    private Integer totalVacantes;

    @Column(nullable = false)
    private Integer vacantesOcupadas;   // matrículas CONFIRMADAS

    @Column(nullable = false)
    private Integer vacantesReservadas; // matrículas en proceso, aún no confirmadas

    // "DISPONIBLE" | "LLENA" | "CERRADA"
    private String estado;

    private String usuarioRegistro;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;

    /** totalVacantes - vacantesOcupadas - vacantesReservadas. No se persiste; se expone en el JSON. */
    @Transient
    public int getVacantesDisponibles() {
        return valor(totalVacantes) - valor(vacantesOcupadas) - valor(vacantesReservadas);
    }

    /** DISPONIBLE / LLENA según los cupos; CERRADA solo cambia por decisión explícita. */
    public void recalcularEstado() {
        if (!CERRADA.equals(estado)) {
            estado = getVacantesDisponibles() > 0 ? DISPONIBLE : LLENA;
        }
    }

    private static int valor(Integer n) {
        return n == null ? 0 : n;
    }
}
