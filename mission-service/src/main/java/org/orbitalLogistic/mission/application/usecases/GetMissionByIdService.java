package org.orbitalLogistic.mission.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.mission.application.ports.in.GetMissionByIdUseCase;
import org.orbitalLogistic.mission.application.ports.out.MissionRepository;
import org.orbitalLogistic.mission.domain.model.Mission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetMissionByIdService implements GetMissionByIdUseCase {

    private final MissionRepository missionRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Mission> getMissionById(Long id) {
        log.debug("Finding mission by id: {}", id);
        return missionRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        log.debug("Checking if mission exists by id: {}", id);
        return missionRepository.existsById(id);
    }
}
