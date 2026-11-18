package com.thales_mileto.pagos.kafka;

import com.thales_mileto.pagos.event.PagoEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PagoProducer {

    private static final Logger log = LoggerFactory.getLogger(PagoProducer.class);
    private static final String TOPIC = "pagos.eventos";

    private final KafkaTemplate<String, PagoEvent> kafkaTemplate;

    public void publicar(PagoEvent evento) {
        kafkaTemplate.send(TOPIC, evento.getPagoId(), evento)
                .whenComplete((resultado, ex) -> {
                    if (ex != null) {
                        log.error("No se pudo publicar {} para pago {}",
                                evento.getEvento(), evento.getPagoId(), ex);
                        return;
                    }

                    log.info("Evento publicado: {} / pago={} / partition={} / offset={}",
                            evento.getEvento(),
                            evento.getPagoId(),
                            resultado.getRecordMetadata().partition(),
                            resultado.getRecordMetadata().offset());
                });
    }
}
