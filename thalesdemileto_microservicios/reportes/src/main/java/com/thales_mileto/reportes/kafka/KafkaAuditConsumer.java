package com.thales_mileto.reportes.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thales_mileto.reportes.model.EventoAuditoria;
import com.thales_mileto.reportes.service.EventoAuditoriaService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KafkaAuditConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaAuditConsumer.class);

    private final EventoAuditoriaService eventoAuditoriaService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = {
                    "matricula.eventos",
                    "academico.notas.eventos",
                    "pagos.eventos"
            },
            groupId = "reportes-service"
    )
    public void consumir(ConsumerRecord<String, String> record) {

        try {
            JsonNode evento = objectMapper.readTree(record.value());

            String tipoEvento = text(evento, "evento");

            if (tipoEvento == null || tipoEvento.isBlank()) {
                log.warn(
                        "Evento sin campo 'evento'. topic={}, offset={}",
                        record.topic(),
                        record.offset()
                );
                return;
            }

            String microservicio = identificarMicroservicio(record.topic());
            String entidad = identificarEntidad(record.topic(), evento);
            String entidadId = obtenerEntidadId(evento);
            String accion = normalizarAccion(tipoEvento);

            Map<String, Object> datos =
                    objectMapper.convertValue(
                            evento,
                            new TypeReference<Map<String, Object>>() {}
                    );

            String usuarioId = text(evento, "usuarioId");
            String correlationId = text(evento, "correlationId");

            EventoAuditoria auditoria =
                    eventoAuditoriaService.registrarEvento(
                            usuarioId,
                            microservicio,
                            tipoEvento,
                            entidad,
                            entidadId,
                            accion,
                            datos,
                            null,
                            correlationId
                    );

            log.info(
                    "Evento registrado en auditoría. topic={}, evento={}, entidad={}, entidadId={}, auditoriaId={}",
                    record.topic(),
                    tipoEvento,
                    entidad,
                    entidadId,
                    auditoria.getId()
            );

        } catch (Exception e) {
            log.error(
                    "Error procesando evento Kafka para Reportes. topic={}, partition={}, offset={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    e
            );
        }
    }

    private String identificarMicroservicio(String topic) {
        if (topic.startsWith("matricula")) {
            return "matricula";
        }

        if (topic.startsWith("academico")) {
            return "academico";
        }

        if (topic.startsWith("pagos")) {
            return "pagos";
        }

        return "desconocido";
    }

    private String identificarEntidad(
            String topic,
            JsonNode evento
    ) {
        if (topic.startsWith("matricula")) {
            return "matricula";
        }

        if (topic.startsWith("academico")) {
            if (evento.hasNonNull("notaId")) {
                return "nota";
            }

            if (evento.hasNonNull("asistenciaId")) {
                return "asistencia";
            }

            return "academico";
        }

        if (topic.startsWith("pagos")) {
            return "pago";
        }

        return "evento";
    }

    private String obtenerEntidadId(JsonNode evento) {

        String[] candidatos = {
                "matriculaId",
                "pagoId",
                "notaId",
                "asistenciaId",
                "entidadId",
                "id"
        };

        for (String campo : candidatos) {
            String valor = text(evento, campo);

            if (valor != null && !valor.isBlank()) {
                return valor;
            }
        }

        return null;
    }

    private String normalizarAccion(String tipoEvento) {

        String evento = tipoEvento.trim().toUpperCase();

        if (evento.contains("CREAD")
                || evento.contains("REGISTR")
                || evento.contains("GENER")) {
            return "CREAR";
        }

        if (evento.contains("ACTUALIZ")
                || evento.contains("MODIFIC")) {
            return "ACTUALIZAR";
        }

        if (evento.contains("ELIMIN")
                || evento.contains("ANUL")) {
            return "ELIMINAR";
        }

        if (evento.contains("CONFIRM")) {
            return "CONFIRMAR";
        }

        if (evento.contains("RECHAZ")) {
            return "RECHAZAR";
        }

        if (evento.contains("REEMBOLS")) {
            return "REEMBOLSAR";
        }

        return evento;
    }

    private String text(JsonNode nodo, String campo) {
        JsonNode valor = nodo.get(campo);

        if (valor == null || valor.isNull()) {
            return null;
        }

        return valor.asText();
    }
}
