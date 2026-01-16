package org.orbitalLogistic.cargo.infrastructure.adapters.in.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.cargo.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ message listener for cargo-service.
 * Listens to events from global Topic Exchange based on routing keys configured in RabbitMQConfig.
 * TODO: Replace placeholder message handling with actual business logic
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQListener {

    /**
     * Listens to all messages from the cargo-service queue.
     * Messages are routed to this queue based on routing keys (bindings) configured in RabbitMQConfig.
     * TODO: Replace with actual message types and processing logic
     * 
     * @param message The message received from RabbitMQ
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleMessage(Object message) {
        log.info("Received message in cargo-service: {}", message);
        
        // TODO: Implement actual message processing logic here
        // You can check the message type or routing key to determine which event it is
        // Example:
        // if (message instanceof MissionCreatedEvent) {
        //     handleMissionCreated((MissionCreatedEvent) message);
        // } else if (message instanceof CargoUpdatedEvent) {
        //     handleCargoUpdated((CargoUpdatedEvent) message);
        // }
    }

    // TODO: Add specific handlers for each event type
    // private void handleMissionCreated(MissionCreatedEvent event) { ... }
    // private void handleCargoUpdated(CargoUpdatedEvent event) { ... }
}

