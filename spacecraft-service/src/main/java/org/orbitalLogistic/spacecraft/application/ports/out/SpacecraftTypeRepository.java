package org.orbitalLogistic.spacecraft.application.ports.out;

import org.orbitalLogistic.spacecraft.domain.model.SpacecraftType;

import java.util.List;
import java.util.Optional;

public interface SpacecraftTypeRepository {
    SpacecraftType save(SpacecraftType spacecraftType);
    Optional<SpacecraftType> findById(Long id);
    List<SpacecraftType> findAll(int limit, int offset);
    long countAll();
    boolean existsById(Long id);
    void deleteById(Long id);
}

