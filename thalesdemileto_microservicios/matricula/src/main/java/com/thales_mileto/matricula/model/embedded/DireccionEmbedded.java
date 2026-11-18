package com.thales_mileto.matricula.model.embedded;

import jakarta.persistence.Embeddable;
import lombok.*;

/** Dirección guardada como columnas de la tabla dueña (estudiantes, apoderados). */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Embeddable
public class DireccionEmbedded {
    private String calle;
    private String numero;
    private String distrito;
    private String provincia;
    private String departamento;
    private String codigoPostal;
    private String referencia;
}
