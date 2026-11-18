package com.thales_mileto.matricula.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "detalles_matricula",
        uniqueConstraints = @UniqueConstraint(name = "uk_detalle_matricula_curso",
                columnNames = {"matricula_id", "curso_id"}),
        indexes = @Index(name = "idx_detalle_matricula", columnList = "matricula_id"))
public class DetalleMatricula {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @Column(name = "matricula_id")
    private String matriculaId;

    @Column(name = "curso_id")
    private String cursoId;
    private String nombreCurso;        // desnormalizado
    private String codigoCurso;        // desnormalizado
    private Integer horasSemanales;

    // "OBLIGATORIO" | "ELECTIVO"
    private String tipoInscripcion;

    // "INSCRITO" | "RETIRADO" | "APROBADO" | "DESAPROBADO"
    private String estado;

    private String usuarioRegistro;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}
