package org.orbitalLogistic.spacecraft.application.ports.out;

import org.orbitalLogistic.spacecraft.domain.model.Spacecraft;

import java.util.List;
import java.util.Optional;

public interface SpacecraftRepository {
    Spacecraft save(Spacecraft spacecraft);
    Optional<Spacecraft> findById(Long id);
    List<Spacecraft> findWithFilters(String name, String status, int limit, int offset);
    long countWithFilters(String name, String status);
    List<Spacecraft> findAvailableForMission();
    boolean existsById(Long id);
    boolean existsByRegistryCode(String registryCode);
    void deleteById(Long id);
}

