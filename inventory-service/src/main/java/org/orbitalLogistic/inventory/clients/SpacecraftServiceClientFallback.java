package org.orbitalLogistic.inventory.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SpacecraftServiceClientFallback implements SpacecraftServiceClient {

    @Override
    public SpacecraftDTO getSpacecraftById(Long id) {
        log.warn("Fallback: Unable to fetch spacecraft with id: {}", id);
        return null;
    }

    @Override
    public Boolean spacecraftExists(Long id) {
        log.warn("Fallback: Unable to check if spacecraft exists with id: {}", id);
        return false;
    }
}

