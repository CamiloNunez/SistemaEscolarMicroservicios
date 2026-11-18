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
@Document(collection = "plantillas_notificacion")
public class PlantillaNotificacion {

    @Id
    private String id;

    @Indexed(unique = true)
    private String codigo; // Ej: MATRICULA_CONFIRMADA, PAGO_CONFIRMADO, PAGO_PENDIENTE, PENSION_VENCIDA

    private String nombre;

    private String asunto;

    private String contenido;

    private CanalNotificacion canal;

    private String evento;

    private Boolean activo = true;

    private LocalDateTime fechaRegistro;
}