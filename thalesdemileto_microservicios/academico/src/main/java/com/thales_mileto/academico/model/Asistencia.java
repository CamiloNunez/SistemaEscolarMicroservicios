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
@Document(collection = "asistencias")
public class Asistencia {

    @Id
    private String id;

    private String estudianteId;

    private String cursoId;

    private String docenteId;

    private String seccionId;

    private LocalDateTime fecha;

    private EstadoAsistencia estado;

    private String observacion;

    private LocalDateTime fechaRegistro;
}
