package com.thales_mileto.matricula.event;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatriculaCreadaEvent {

    private String evento;
    private String matriculaId;
    private String estudianteId;
    private String nombreEstudiante;
    private String periodo;
    private String grado;
    private String seccion;
    private String usuarioRegistro;
    private LocalDateTime fechaRegistro;
}
