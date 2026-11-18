package com.thales_mileto.notificaciones.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "preferencias_notificacion")
public class PreferenciaNotificacion {

    @Id
    private String id;

    @Indexed(unique = true)
    private String usuarioId;

    private Boolean correoHabilitado = true;

    private Boolean pushHabilitado = true;

    private Boolean notificacionesPago = true;

    private Boolean notificacionesMatricula = true;

    private Boolean notificacionesAcademicas = true;

    private LocalDateTime fechaActualizacion;
}