package com.thales_mileto.notificaciones.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * usuarioId apunta conceptualmente al Usuario de login, pero NO copiar
 * Usuario.java aquí. Solo se guarda la referencia (id) y el destino de entrega.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "destinatarios_notificacion")
public class DestinatarioNotificacion {

    @Id
    private String id;

    private String notificacionId;

    private String usuarioId;

    private String direccionDestino; // Ej: correo, token push, número de celular

    private String tipoDestinatario; // Ej: ESTUDIANTE, PADRE, DOCENTE, ADMINISTRATIVO

    private String estadoEntrega; // Ej: PENDIENTE, ENTREGADO, FALLIDO

    private LocalDateTime fechaEntrega;
}