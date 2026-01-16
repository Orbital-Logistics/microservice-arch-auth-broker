package org.orbitalLogistic.messageproducer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.messageproducer.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for producing messages to RabbitMQ using pub/sub pattern.
 * Producer doesn't know which services consume messages - it just publishes events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProducerService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publishes an event to the topic exchange.
     * Producer doesn't need to know which services will consume this event.
     * 
     * @param routingKey The routing key (event name), e.g., "mission.created", "cargo.updated"
     * @param event The event payload
     */
    public void publishEvent(String routingKey, Object event) {
        log.info("Publishing event with routing key: {}, event: {}", routingKey, event);
        rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE_NAME, routingKey, event);
    }

    /**
     * Convenience methods for common events.
     * These are optional - you can use publishEvent() directly.
     */
    
    public void publishMissionCreated(Object event) {
        publishEvent(RabbitMQConfig.ROUTING_KEY_MISSION_CREATED, event);
    }

    public void publishMissionUpdated(Object event) {
        publishEvent(RabbitMQConfig.ROUTING_KEY_MISSION_UPDATED, event);
    }

    public void publishCargoCreated(Object event) {
        publishEvent(RabbitMQConfig.ROUTING_KEY_CARGO_CREATED, event);
    }

    public void publishCargoUpdated(Object event) {
        publishEvent(RabbitMQConfig.ROUTING_KEY_CARGO_UPDATED, event);
    }

    public void publishUserCreated(Object event) {
        publishEvent(RabbitMQConfig.ROUTING_KEY_USER_CREATED, event);
    }

    public void publishUserUpdated(Object event) {
        publishEvent(RabbitMQConfig.ROUTING_KEY_USER_UPDATED, event);
    }

    public void publishSpacecraftCreated(Object event) {
        publishEvent(RabbitMQConfig.ROUTING_KEY_SPACECRAFT_CREATED, event);
    }

    public void publishSpacecraftUpdated(Object event) {
        publishEvent(RabbitMQConfig.ROUTING_KEY_SPACECRAFT_UPDATED, event);
    }

    public void publishInventoryTransaction(Object event) {
        publishEvent(RabbitMQConfig.ROUTING_KEY_INVENTORY_TRANSACTION, event);
    }

    public void publishMaintenanceLog(Object event) {
        publishEvent(RabbitMQConfig.ROUTING_KEY_MAINTENANCE_LOG, event);
    }
}

