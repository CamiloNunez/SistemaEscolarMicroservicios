package com.thales_mileto.matricula.kafka;

import com.thales_mileto.matricula.event.MatriculaCreadaEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatriculaProducer {

    private final KafkaTemplate<String, MatriculaCreadaEvent> kafkaTemplate;

    private static final String TOPIC = "matricula.eventos";

    public void enviarEvento(MatriculaCreadaEvent evento) {
        kafkaTemplate.send(
            TOPIC,
            evento.getMatriculaId(),
            evento
        );
    }
}
