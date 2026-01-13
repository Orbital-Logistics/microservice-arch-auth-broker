package org.orbitalLogistic.spacecraft.application.ports.in;

import org.orbitalLogistic.spacecraft.domain.model.Spacecraft;

import java.util.List;

public interface GetSpacecraftsUseCase {
    List<Spacecraft> getSpacecrafts(String name, String status, int limit, int offset);
    long countSpacecrafts(String name, String status);
    Spacecraft getSpacecraftById(Long id);
    List<Spacecraft> getAvailableSpacecrafts();
    boolean spacecraftExists(Long id);
}

