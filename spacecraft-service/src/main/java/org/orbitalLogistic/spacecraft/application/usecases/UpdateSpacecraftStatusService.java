package org.orbitalLogistic.spacecraft.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.spacecraft.application.ports.in.UpdateSpacecraftStatusUseCase;
import org.orbitalLogistic.spacecraft.application.ports.out.SpacecraftRepository;
import org.orbitalLogistic.spacecraft.domain.model.Spacecraft;
import org.orbitalLogistic.spacecraft.domain.model.enums.SpacecraftStatus;
import org.orbitalLogistic.spacecraft.exceptions.SpacecraftNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateSpacecraftStatusService implements UpdateSpacecraftStatusUseCase {

    private final SpacecraftRepository spacecraftRepository;

    @Override
    public Spacecraft updateStatus(Long id, SpacecraftStatus status) {
        log.debug("Updating spacecraft status - id: {}, status: {}", id, status);

        Spacecraft spacecraft = spacecraftRepository.findById(id)
                .orElseThrow(() -> new SpacecraftNotFoundException("Spacecraft not found with id: " + id));

        Spacecraft updated = spacecraft.toBuilder()
                .status(status)
                .build();

        updated.validate();

        Spacecraft saved = spacecraftRepository.save(updated);
        log.info("Updated spacecraft status - id: {}, new status: {}", id, status);

        return saved;
    }

    @Override
    public Spacecraft changeLocation(Long id, String newLocation) {
        log.debug("Changing spacecraft location - id: {}, location: {}", id, newLocation);

        Spacecraft spacecraft = spacecraftRepository.findById(id)
                .orElseThrow(() -> new SpacecraftNotFoundException("Spacecraft not found with id: " + id));

        Spacecraft updated = spacecraft.toBuilder()
                .currentLocation(newLocation)
                .build();

        updated.validate();

        Spacecraft saved = spacecraftRepository.save(updated);
        log.info("Changed spacecraft location - id: {}, new location: {}", id, newLocation);

        return saved;
    }
}

