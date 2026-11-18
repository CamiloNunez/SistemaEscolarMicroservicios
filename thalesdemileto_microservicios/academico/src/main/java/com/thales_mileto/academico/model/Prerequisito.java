package com.thales_mileto.academico.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Document(collection = "prerequisitos")
public class Prerequisito {

    @Id
    private String id;

    private String cursoId;

    private String cursoPrerequisitoId;

    private String tipo; // Ej: OBLIGATORIO, RECOMENDADO

    private String estado; // Ej: ACTIVO, INACTIVO
}