package com.thales_mileto.reportes.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reportes_generados")
public class ReporteGenerado {

    @Id
    private String id;

    private String tipoReporte; // Ej: PAGOS_MENSUAL, ASISTENCIA_CURSO, NOTAS_PERIODO

    private String usuarioSolicitanteId;

    private Map<String, Object> parametros;

    private String formato; // Ej: PDF, EXCEL, CSV

    private String rutaArchivo;

    private String estado; // Ej: SOLICITADO, GENERANDO, LISTO, ERROR

    private LocalDateTime fechaSolicitud;

    private LocalDateTime fechaGeneracion;
}