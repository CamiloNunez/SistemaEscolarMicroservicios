package com.thales_mileto.matricula.model;

import com.thales_mileto.matricula.model.embedded.ReferenciaMigracion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "matriculas",
        uniqueConstraints = @UniqueConstraint(name = "uk_matricula_referencia_migracion",
                columnNames = {"sistema_origen", "codigo_externo"}),
        indexes = {
                @Index(name = "idx_matricula_estudiante", columnList = "estudiante_id"),
                @Index(name = "idx_matricula_seccion_periodo",
                        columnList = "seccion_id, periodo_academico_id")
        })
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @Column(name = "estudiante_id")
    private String estudianteId;
    private String nombreEstudiante;
    private String grado;
    private String seccion;
    private String periodo;
    private String estado;

    /**
     * Ids de sección y periodo académico que identifican la Vacante que consume
     * esta matrícula. Si vienen informados, crear/actualizar/eliminar la matrícula
     * ajusta los cupos de la vacante en la misma transacción (ver MatriculaService).
     * Si son null (p. ej. matrículas históricas migradas), no se controla cupo.
     */
    @Column(name = "seccion_id", length = 36)
    private String seccionId;

    @Column(name = "periodo_academico_id", length = 36)
    private String periodoAcademicoId;

    private String usuarioRegistro;
    private LocalDateTime fechaRegistro;

    /**
     * Presente solo en matrículas importadas desde otro sistema.
     * (sistemaOrigen, codigoExterno) es único: uk_matricula_referencia_migracion.
     */
    @Embedded
    private ReferenciaMigracion referenciaMigracion;
}
