package com.thales_mileto.academico.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Conecta docente + curso + seccion + periodo sin duplicar Curso ni Seccion
 * (que ya viven en el microservicio matricula).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "asignaciones_docente")
public class AsignacionDocente {

    @Id
    private String id;

    private String docenteId;

    private String cursoId;

    private String seccionId;

    private String periodoAcademicoId;

    private String estado; // Ej: ACTIVA, FINALIZADA, CANCELADA

    private LocalDateTime fechaAsignacion;
}
