package com.thales_mileto.academico.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "notas")
public class Nota {

    @Id
    private String id;

    private String estudianteId;

    private String cursoId;

    private String periodoAcademicoId;

    private String docenteId;

    private String tipoEvaluacion; // Ej: PARCIAL, FINAL, PRACTICA, TRABAJO

    private String descripcion;

    private Double calificacion;

    private LocalDateTime fechaEvaluacion;

    private String estado; // Ej: REGISTRADA, MODIFICADA, ANULADA

    private LocalDateTime fechaRegistro;
}