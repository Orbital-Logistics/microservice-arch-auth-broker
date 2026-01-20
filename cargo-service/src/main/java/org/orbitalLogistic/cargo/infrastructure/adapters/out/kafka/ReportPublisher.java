package org.orbitalLogistic.cargo.infrastructure.adapters.out.kafka;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.cargo.application.ports.out.ReportSender;
import org.orbitalLogistic.cargo.domain.model.CargoStorage;
import org.springframework.stereotype.Component;
import org.springframework.kafka.core.KafkaTemplate;

@Component
@RequiredArgsConstructor
public class ReportPublisher implements ReportSender {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void send(CargoStorage storage) {
        kafkaTemplate.send("cargo-reports-data", storage);
    }
}

