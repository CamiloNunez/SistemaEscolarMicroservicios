package com.thales_mileto.academico.kafka;

import com.thales_mileto.academico.event.NotaAcademicaEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotaAcademicaProducer {

    private static final Logger log = LoggerFactory.getLogger(NotaAcademicaProducer.class);
    private static final String TOPIC = "academico.notas.eventos";

    private final KafkaTemplate<String, NotaAcademicaEvent> kafkaTemplate;

    public void publicar(NotaAcademicaEvent evento) {
        kafkaTemplate.send(TOPIC, evento.getNotaId(), evento)
                .whenComplete((resultado, ex) -> {
                    if (ex != null) {
                        log.error("No se pudo publicar {} para nota {}",
                                evento.getEvento(), evento.getNotaId(), ex);
                        return;
                    }
                    log.info("Evento publicado: {} / nota={} / topic={} / partition={} / offset={}",
                            evento.getEvento(), evento.getNotaId(),
                            resultado.getRecordMetadata().topic(),
                            resultado.getRecordMetadata().partition(),
                            resultado.getRecordMetadata().offset());
                });
    }
}
