package com.thales_mileto.matricula.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "grados", uniqueConstraints =
        @UniqueConstraint(name = "uk_grado_programa_numero",
                columnNames = {"programa_academico_id", "numero"}))
public class Grado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @Column(name = "programa_academico_id")
    private String programaAcademicoId;

    private Integer numero;           // 1, 2, 3, ...
    private String nombre;            // "Primer Grado", "1°"
    private String descripcion;

    // Edad mínima/máxima recomendada
    private Integer edadMinima;
    private Integer edadMaxima;

    // "ACTIVO" | "INACTIVO"
    private String estado;

    private String usuarioRegistro;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}
