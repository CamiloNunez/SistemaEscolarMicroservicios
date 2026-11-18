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
@Document(collection = "eventos_auditoria")
public class EventoAuditoria {

    @Id
    private String id;

    private String usuarioId;

    private String microservicio; // Ej: login, matricula, pagos, academico, notificaciones

    private String tipoEvento; // Ej: MATRICULA_CREADA, PAGO_INICIADO, PAGO_CONFIRMADO, NOTA_REGISTRADA, USUARIO_CREADO

    private String entidad;

    private String entidadId;

    private String accion; // Ej: CREAR, ACTUALIZAR, ELIMINAR, CONSULTAR

    private Map<String, Object> datos;

    private String ip;

    private LocalDateTime fecha;

    private String correlationId;
}