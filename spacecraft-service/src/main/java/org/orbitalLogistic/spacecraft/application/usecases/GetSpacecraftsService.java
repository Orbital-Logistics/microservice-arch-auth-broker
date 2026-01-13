package org.orbitalLogistic.spacecraft.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.spacecraft.application.ports.in.GetSpacecraftsUseCase;
import org.orbitalLogistic.spacecraft.application.ports.out.SpacecraftRepository;
import org.orbitalLogistic.spacecraft.domain.model.Spacecraft;
import org.orbitalLogistic.spacecraft.exceptions.SpacecraftNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetSpacecraftsService implements GetSpacecraftsUseCase {

    private final SpacecraftRepository spacecraftRepository;

    @Override
    public List<Spacecraft> getSpacecrafts(String name, String status, int limit, int offset) {
        log.debug("Getting spacecrafts with filters - name: {}, status: {}, limit: {}, offset: {}",
                name, status, limit, offset);
        return spacecraftRepository.findWithFilters(name, status, limit, offset);
    }

    @Override
    public long countSpacecrafts(String name, String status) {
        return spacecraftRepository.countWithFilters(name, status);
    }

    @Override
    public Spacecraft getSpacecraftById(Long id) {
        log.debug("Getting spacecraft by id: {}", id);
        return spacecraftRepository.findById(id)
                .orElseThrow(() -> new SpacecraftNotFoundException("Spacecraft not found with id: " + id));
    }

    @Override
    public List<Spacecraft> getAvailableSpacecrafts() {
        log.debug("Getting available spacecrafts");
        return spacecraftRepository.findAvailableForMission();
    }

    @Override
    public boolean spacecraftExists(Long id) {
        return spacecraftRepository.existsById(id);
    }
}

