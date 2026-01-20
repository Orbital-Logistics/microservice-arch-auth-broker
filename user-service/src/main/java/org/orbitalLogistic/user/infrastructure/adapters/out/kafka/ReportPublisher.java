package org.orbitalLogistic.user.infrastructure.adapters.out.kafka;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.user.application.ports.in.CreateUserCommand;
import org.orbitalLogistic.user.application.ports.in.RegisterCommand;
import org.orbitalLogistic.user.application.ports.out.ReportSender;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportPublisher implements ReportSender {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void send(RegisterCommand user) {
        RegisterCommand censored = new RegisterCommand(user.username(), "", user.email(), user.roleIds());
        kafkaTemplate.send("user-reports-data", censored);
    }
}
