package com.thales_mileto.matricula.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Rastro de un registro importado desde otro sistema (sistema anterior, Excel, SIAGIE, etc.).
 * Permite ubicar el registro original, evitar importarlo dos veces y conservar
 * los datos tal como venían aunque no tengan equivalente en el modelo actual.
 *
 * Se guarda como columnas de la tabla dueña; (sistema_origen, codigo_externo) es UNIQUE
 * en matriculas, estudiantes y apoderados. PostgreSQL permite varios NULL en un UNIQUE,
 * así que los registros no migrados no chocan entre sí.
 */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Embeddable
public class ReferenciaMigracion {

    /** Sistema del que proviene el registro. Ej: "SISTEMA_ANTERIOR", "EXCEL_2024", "SIAGIE". */
    @Column(name = "sistema_origen")
    private String sistemaOrigen;

    /** Identificador del registro en el sistema de origen. Único por sistemaOrigen. */
    @Column(name = "codigo_externo")
    private String codigoExterno;

    private LocalDateTime fechaMigracion;

    /** Lote o archivo de importación, útil para auditar o revertir una carga. */
    private String loteMigracion;

    private String observacion;

    /** Datos originales sin transformar (ej. DNI del apoderado o código de aula del sistema anterior). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> datosOriginales;
}
