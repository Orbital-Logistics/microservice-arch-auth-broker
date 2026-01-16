package org.orbitalLogistic.mission.infrastructure.adapters.out.kafka;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.mission.application.ports.in.CreateMissionCommand;
import org.orbitalLogistic.mission.application.ports.out.ReportSender;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportPublisher implements ReportSender {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void send(CreateMissionCommand rental) {
        kafkaTemplate.send("reports-data", rental);
    }
}
