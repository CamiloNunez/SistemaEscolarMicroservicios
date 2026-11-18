package com.thales_mileto.academico.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotaAcademicaEvent {
    private String evento;
    private String notaId;
    private String estudianteId;
    private String cursoId;
    private String periodoAcademicoId;
    private String docenteId;
    private String tipoEvaluacion;
    private String descripcion;
    private Double calificacion;
    private String estado;
    private LocalDateTime fechaEvaluacion;
    private LocalDateTime fechaRegistro;
}
