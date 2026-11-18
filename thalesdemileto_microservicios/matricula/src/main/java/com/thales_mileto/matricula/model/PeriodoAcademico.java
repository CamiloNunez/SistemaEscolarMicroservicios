package com.thales_mileto.matricula.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "periodos_academicos",
        uniqueConstraints = @UniqueConstraint(name = "uk_periodo_codigo", columnNames = "codigo"),
        indexes = @Index(name = "idx_periodo_estado", columnList = "estado"))
public class PeriodoAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    private String codigo;       // Ej: "2025-I", "2025"

    private String nombre;       // Ej: "Año Escolar 2025"
    private String tipo;         // "ANUAL" | "SEMESTRAL" | "TRIMESTRAL"

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    // Ventana de matrícula
    private LocalDate inicioMatricula;
    private LocalDate finMatricula;
    private LocalDate inicioMatriculaExtemporanea;
    private LocalDate finMatriculaExtemporanea;

    // "PLANIFICADO" | "MATRICULA" | "EN_CURSO" | "CERRADO"
    private String estado;

    private String usuarioRegistro;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}
