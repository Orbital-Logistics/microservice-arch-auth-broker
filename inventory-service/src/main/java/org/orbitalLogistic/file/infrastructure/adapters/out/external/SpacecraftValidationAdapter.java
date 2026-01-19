package org.orbitalLogistic.file.infrastructure.adapters.out.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.file.application.ports.out.SpacecraftValidationPort;
import org.orbitalLogistic.file.clients.resilient.ResilientSpacecraftService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpacecraftValidationAdapter implements SpacecraftValidationPort {

    private final ResilientSpacecraftService spacecraftService;

    @Override
    public boolean spacecraftExists(Long spacecraftId) {
        try {
            return spacecraftService.spacecraftExists(spacecraftId);
        } catch (Exception e) {
            log.error("Error validating spacecraft existence for id: {}", spacecraftId, e);
            return false;
        }
    }
}
