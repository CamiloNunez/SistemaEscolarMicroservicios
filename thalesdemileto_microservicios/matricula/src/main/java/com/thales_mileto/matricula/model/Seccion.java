package com.thales_mileto.matricula.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "secciones", uniqueConstraints =
        @UniqueConstraint(name = "uk_seccion_grado_letra_periodo",
                columnNames = {"grado_id", "letra", "periodo_academico_id"}))
public class Seccion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @Column(name = "grado_id")
    private String gradoId;
    @Column(name = "periodo_academico_id")
    private String periodoAcademicoId;

    private String letra;             // "A", "B", "C"
    private String nombre;            // "1° A", calculado o manual
    private String turno;             // "MAÑANA" | "TARDE" | "NOCHE"
    private Integer capacidadMaxima;
    private String aulaAsignada;

    // Docente tutor (referencia)
    private String docenteTutorId;

    // "ACTIVO" | "INACTIVO" | "COMPLETA"
    private String estado;

    private String usuarioRegistro;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}
