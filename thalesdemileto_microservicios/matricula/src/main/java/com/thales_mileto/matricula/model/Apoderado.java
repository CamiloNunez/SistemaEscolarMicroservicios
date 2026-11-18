package com.thales_mileto.matricula.model;

import com.thales_mileto.matricula.model.embedded.DireccionEmbedded;
import com.thales_mileto.matricula.model.embedded.ReferenciaMigracion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "apoderados", uniqueConstraints = {
        @UniqueConstraint(name = "uk_apoderado_dni", columnNames = "dni"),
        // Un usuario de login solo puede corresponder a un apoderado.
        @UniqueConstraint(name = "uk_apoderado_usuario", columnNames = "usuario_id"),
        // Un apoderado del sistema de origen solo se importa una vez.
        @UniqueConstraint(name = "uk_apoderado_referencia_migracion",
                columnNames = {"sistema_origen", "codigo_externo"})
})
public class Apoderado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;

    private String dni;

    // "PADRE" | "MADRE" | "TUTOR_LEGAL" | "ABUELO" | "OTRO"
    private String tipoRelacion;
    private Boolean esApoderadoPrincipal;

    // Contacto
    private String telefonoCelular;
    private String telefonoFijo;
    private String correo;

    @Embedded
    private DireccionEmbedded direccion;

    // Datos laborales (útil para emergencias y comunicaciones)
    private String ocupacion;
    private String centroTrabajo;
    private String telefonoTrabajo;

    // Estado: "ACTIVO" | "INACTIVO"
    private String estado;

    /**
     * Id del usuario PADRE en el microservicio login. Se asigna solo mediante
     * los endpoints de vinculación, que validan tipo de usuario y DNI.
     * Único: uk_apoderado_usuario.
     */
    @Column(name = "usuario_id")
    private String usuarioId;

    /** Presente si el apoderado fue importado desde otro sistema. */
    @Embedded
    private ReferenciaMigracion referenciaMigracion;

    private String usuarioRegistro;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}
