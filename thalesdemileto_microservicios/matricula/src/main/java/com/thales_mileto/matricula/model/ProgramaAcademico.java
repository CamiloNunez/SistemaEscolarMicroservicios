package com.thales_mileto.matricula.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "programas_academicos", uniqueConstraints =
        @UniqueConstraint(name = "uk_programa_codigo", columnNames = "codigo"))
public class ProgramaAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    private String codigo;        // Ej: "PRIMARIA", "SECUNDARIA"

    private String nombre;        // Ej: "Educación Primaria"
    private String descripcion;
    private Integer duracionAnios;

    // "INICIAL" | "PRIMARIA" | "SECUNDARIA"
    private String nivelEducativo;

    // "ACTIVO" | "INACTIVO"
    private String estado;

    private String usuarioRegistro;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}
