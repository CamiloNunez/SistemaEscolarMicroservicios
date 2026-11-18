package com.thales_mileto.matricula.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "cursos", uniqueConstraints =
        @UniqueConstraint(name = "uk_curso_codigo", columnNames = "codigo"))
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    private String codigo;         // Ej: "MAT", "COM", "CTA"

    private String nombre;         // "Matemática", "Comunicación"
    private String descripcion;
    private String area;           // Área curricular: "CIENCIAS", "LETRAS", etc.

    // "OBLIGATORIO" | "ELECTIVO"
    private String tipo;

    private Integer horasSemanales;

    // "ACTIVO" | "INACTIVO"
    private String estado;

    private String usuarioRegistro;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}
