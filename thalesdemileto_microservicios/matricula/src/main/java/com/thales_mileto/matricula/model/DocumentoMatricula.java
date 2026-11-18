package com.thales_mileto.matricula.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "documentos_matricula",
        indexes = @Index(name = "idx_documento_matricula", columnList = "matricula_id"))
public class DocumentoMatricula {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @Column(name = "matricula_id")
    private String matriculaId;

    // "DNI" | "PARTIDA_NACIMIENTO" | "LIBRETA_NOTAS" | "FOTO" |
    // "FICHA_SALUD" | "CONSTANCIA_TRASLADO" | "OTRO"
    private String tipoDocumento;

    private String nombreArchivo;

    @Column(length = 1000)
    private String rutaAlmacenamiento;  // path en S3, GCS, etc.

    @Column(length = 1000)
    private String urlAcceso;

    private String mimeType;

    // Columna con nombre ASCII para evitar la "ñ" en SQL.
    @Column(name = "tamano_bytes")
    private Long tamañoBytes;

    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private Boolean esObligatorio;

    // "PENDIENTE" | "ENTREGADO" | "VERIFICADO" | "RECHAZADO"
    private String estado;

    @Column(length = 2000)
    private String observaciones;
    private String usuarioVerificacion;
    private LocalDateTime fechaVerificacion;

    private String usuarioRegistro;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}
