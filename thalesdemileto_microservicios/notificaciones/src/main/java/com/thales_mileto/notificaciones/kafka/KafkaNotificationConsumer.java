package com.thales_mileto.notificaciones.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thales_mileto.notificaciones.model.Notificacion;
import com.thales_mileto.notificaciones.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KafkaNotificationConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaNotificationConsumer.class);

    private final NotificacionService notificacionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = {
                    "matricula.eventos",
                    "academico.notas.eventos",
                    "pagos.eventos"
            },
            groupId = "notificaciones-service"
    )
    public void consumir(ConsumerRecord<String, String> record) {

        try {
            JsonNode evento = objectMapper.readTree(record.value());
            String tipoEvento = text(evento, "evento");

            if (tipoEvento == null || tipoEvento.isBlank()) {
                log.warn("Evento sin campo 'evento'. topic={}, offset={}",
                        record.topic(), record.offset());
                return;
            }

            String referenciaId = obtenerReferenciaId(evento);
            Map<String, String> variables = convertirVariables(evento);

            // Convención: codigo de plantilla = tipo de evento.
            Notificacion notificacion =
                    notificacionService.crearDesdePlantilla(
                            tipoEvento,
                            referenciaId,
                            variables
                    );

            log.info(
                    "Notificación creada desde Kafka. topic={}, evento={}, referenciaId={}, id={}",
                    record.topic(),
                    tipoEvento,
                    referenciaId,
                    notificacion.getId()
            );

        } catch (IllegalArgumentException e) {
            log.warn("Evento no procesado. topic={}, key={}, motivo={}",
                    record.topic(), record.key(), e.getMessage());

        } catch (Exception e) {
            log.error("Error procesando evento Kafka. topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset(), e);
        }
    }

    private String obtenerReferenciaId(JsonNode evento) {
        String[] candidatos = {"matriculaId", "pagoId", "notaId", "id"};

        for (String campo : candidatos) {
            String valor = text(evento, campo);
            if (valor != null && !valor.isBlank()) {
                return valor;
            }
        }

        return null;
    }

    private Map<String, String> convertirVariables(JsonNode evento) {
        Map<String, String> variables = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> campos = evento.fields();

        while (campos.hasNext()) {
            Map.Entry<String, JsonNode> campo = campos.next();
            JsonNode valor = campo.getValue();

            variables.put(
                    campo.getKey(),
                    valor == null || valor.isNull()
                            ? ""
                            : valor.isValueNode()
                                ? valor.asText()
                                : valor.toString()
            );
        }

        return variables;
    }

    private String text(JsonNode nodo, String campo) {
        JsonNode valor = nodo.get(campo);
        return valor == null || valor.isNull() ? null : valor.asText();
    }
}
