package com.thales_mileto.academico.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * IMPORTANTE: no dupliques username/password/email/roles aquí.
 * Esos campos pertenecen a Usuario.java en el microservicio login.
 * usuarioId es solo la referencia conceptual hacia ese usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "docentes")
public class Docente {

    @Id
    private String id;

    private String usuarioId;

    private String codigoDocente;

    private String especialidad;

    private String estado; // Ej: ACTIVO, INACTIVO, LICENCIA

    private LocalDateTime fechaRegistro;

    private LocalDateTime fechaActualizacion;
}