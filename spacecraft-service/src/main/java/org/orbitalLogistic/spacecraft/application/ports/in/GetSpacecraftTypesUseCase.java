package org.orbitalLogistic.spacecraft.application.ports.in;

import org.orbitalLogistic.spacecraft.domain.model.SpacecraftType;

import java.util.List;

public interface GetSpacecraftTypesUseCase {
    List<SpacecraftType> getAllSpacecraftTypes(int limit, int offset);
    long countAllSpacecraftTypes();
    SpacecraftType getSpacecraftTypeById(Long id);
}

