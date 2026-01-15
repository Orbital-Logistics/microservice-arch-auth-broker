package org.orbitalLogistic.messageproducer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.messageproducer.service.MessageProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for message producer service.
 * Uses pub/sub pattern - publishes events without knowing which services consume them.
 */
@Slf4j
@RestController
@RequestMapping("/api/message-producer")
@RequiredArgsConstructor
public class MessageProducerController {

    private final MessageProducerService messageProducerService;

    /**
     * Publishes an event to the topic exchange.
     * Producer doesn't know which services will consume this event.
     * 
     * @param routingKey The routing key (event name), e.g., "mission.created", "cargo.updated"
     * @param event The event payload
     */
    @PostMapping("/publish")
    public ResponseEntity<String> publishEvent(
            @RequestParam String routingKey,
            @RequestBody Object event) {
        messageProducerService.publishEvent(routingKey, event);
        return ResponseEntity.ok("Event published with routing key: " + routingKey);
    }

    /**
     * Convenience endpoints for common events.
     * These are optional - you can use /publish endpoint directly.
     */
    
    @PostMapping("/events/mission/created")
    public ResponseEntity<String> publishMissionCreated(@RequestBody Object event) {
        messageProducerService.publishMissionCreated(event);
        return ResponseEntity.ok("Mission created event published");
    }

    @PostMapping("/events/mission/updated")
    public ResponseEntity<String> publishMissionUpdated(@RequestBody Object event) {
        messageProducerService.publishMissionUpdated(event);
        return ResponseEntity.ok("Mission updated event published");
    }

    @PostMapping("/events/cargo/created")
    public ResponseEntity<String> publishCargoCreated(@RequestBody Object event) {
        messageProducerService.publishCargoCreated(event);
        return ResponseEntity.ok("Cargo created event published");
    }

    @PostMapping("/events/cargo/updated")
    public ResponseEntity<String> publishCargoUpdated(@RequestBody Object event) {
        messageProducerService.publishCargoUpdated(event);
        return ResponseEntity.ok("Cargo updated event published");
    }

    @PostMapping("/events/user/created")
    public ResponseEntity<String> publishUserCreated(@RequestBody Object event) {
        messageProducerService.publishUserCreated(event);
        return ResponseEntity.ok("User created event published");
    }

    @PostMapping("/events/user/updated")
    public ResponseEntity<String> publishUserUpdated(@RequestBody Object event) {
        messageProducerService.publishUserUpdated(event);
        return ResponseEntity.ok("User updated event published");
    }

    @PostMapping("/events/spacecraft/created")
    public ResponseEntity<String> publishSpacecraftCreated(@RequestBody Object event) {
        messageProducerService.publishSpacecraftCreated(event);
        return ResponseEntity.ok("Spacecraft created event published");
    }

    @PostMapping("/events/spacecraft/updated")
    public ResponseEntity<String> publishSpacecraftUpdated(@RequestBody Object event) {
        messageProducerService.publishSpacecraftUpdated(event);
        return ResponseEntity.ok("Spacecraft updated event published");
    }
}

