package org.orbitalLogistic.mission.infrastructure.adapters.out.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.mission.application.ports.out.SpacecraftServicePort;
import org.orbitalLogistic.mission.clients.SpacecraftDTO;
import org.orbitalLogistic.mission.clients.resilient.ResilientSpacecraftService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpacecraftServiceAdapter implements SpacecraftServicePort {

    private final ResilientSpacecraftService resilientSpacecraftService;

    @Override
    public String getSpacecraftNameById(Long spacecraftId) {
        try {
            SpacecraftDTO spacecraft = resilientSpacecraftService.getSpacecraftById(spacecraftId);
            return spacecraft != null ? spacecraft.name() : "Unknown";
        } catch (Exception e) {
            log.warn("Failed to get spacecraft name for spacecraftId {}: {}", spacecraftId, e.getMessage());
            return "Unknown";
        }
    }

    @Override
    public boolean spacecraftExists(Long spacecraftId) {
        try {
            Boolean exists = resilientSpacecraftService.spacecraftExists(spacecraftId);
            return exists != null && exists;
        } catch (Exception e) {
            log.warn("Failed to check spacecraft existence for spacecraftId {}: {}", spacecraftId, e.getMessage());
            return false;
        }
    }
}
