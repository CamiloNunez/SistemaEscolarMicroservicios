package com.thales_mileto.reportes.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Encaja con los indicadores de la tesis: disponibilidad, tiempo de respuesta,
 * errores, operaciones procesadas, etc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "indicadores")
public class Indicador {

    @Id
    private String id;

    private String codigo; // Ej: DISPONIBILIDAD, TIEMPO_RESPUESTA, TASA_ERROR, OPERACIONES_PROCESADAS

    private String nombre;

    private String tipo; // Ej: DISPONIBILIDAD, RENDIMIENTO, USO, NEGOCIO

    private Double valor;

    private String periodo; // Ej: 2026-08, DIARIO, SEMANAL

    private LocalDateTime fechaCalculo;
}