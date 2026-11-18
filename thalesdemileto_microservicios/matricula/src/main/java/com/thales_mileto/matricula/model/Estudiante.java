package com.thales_mileto.matricula.model;

import com.thales_mileto.matricula.model.embedded.DireccionEmbedded;
import com.thales_mileto.matricula.model.embedded.ReferenciaMigracion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "estudiantes",
        uniqueConstraints = {
                // dni puede ser NULL (menor sin DNI); PostgreSQL permite varios NULL en un UNIQUE.
                @UniqueConstraint(name = "uk_estudiante_dni", columnNames = "dni"),
                // Un estudiante del sistema de origen solo se importa una vez.
                @UniqueConstraint(name = "uk_estudiante_referencia_migracion",
                        columnNames = {"sistema_origen", "codigo_externo"})
        },
        indexes = {
                @Index(name = "idx_estudiante_apellidos_nombre",
                        columnList = "apellido_paterno, apellido_materno, nombres"),
                @Index(name = "idx_estudiante_estado", columnList = "estado")
        })
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    // Datos personales
    private String nombres;
    @Column(name = "apellido_paterno")
    private String apellidoPaterno;
    @Column(name = "apellido_materno")
    private String apellidoMaterno;

    private String dni;                  // puede ser null si es menor sin DNI

    private String codigoEstudiante;     // código interno del colegio
    private LocalDate fechaNacimiento;
    private String genero;               // "M" | "F" | "OTRO"
    private String lugarNacimiento;
    private String nacionalidad;

    // Contacto y ubicación (embebido: siempre se consulta junto al estudiante)
    @Embedded
    private DireccionEmbedded direccion;
    private String telefono;
    private String correo;

    // Datos médicos/especiales relevantes para la matrícula
    private String grupoSanguineo;

    // "DISCAPACIDAD_VISUAL", "TDAH", etc.
    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "estudiante_condiciones_especiales",
            joinColumns = @JoinColumn(name = "estudiante_id"))
    @Column(name = "condicion")
    private List<String> condicionesEspeciales;

    private Boolean requiereAtencionEspecial;

    // Referencias a apoderados (lista porque puede haber padre y madre)
    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "estudiante_apoderados",
            joinColumns = @JoinColumn(name = "estudiante_id"))
    @Column(name = "apoderado_id", length = 36)
    private List<String> apoderadoIds;

    // Estado: "ACTIVO" | "INACTIVO" | "RETIRADO" | "EGRESADO"
    private String estado;

    /** Presente si el estudiante fue importado desde otro sistema. */
    @Embedded
    private ReferenciaMigracion referenciaMigracion;

    private String usuarioRegistro;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}
