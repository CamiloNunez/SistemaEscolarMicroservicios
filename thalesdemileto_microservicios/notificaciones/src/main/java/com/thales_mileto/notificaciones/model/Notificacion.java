package com.thales_mileto.notificaciones.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notificaciones")
public class Notificacion {

    @Id
    private String id;

    private String plantillaId;

    private String tipoEvento;

    private String referenciaId; // Ej: id del pago, matricula, nota, etc. que originó la notificación

    private CanalNotificacion canal;

    private String asunto;

    private String contenido;

    private String estado; // Ej: PENDIENTE, ENVIADA, FALLIDA

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaEnvio;
}