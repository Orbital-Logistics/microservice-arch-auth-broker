package org.orbitalLogistic.mission.application.ports.in;

import org.orbitalLogistic.mission.domain.model.Mission;

import java.util.Optional;

public interface GetMissionByIdUseCase {
    Optional<Mission> getMissionById(Long id);
    boolean existsById(Long id);
}
