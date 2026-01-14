package org.orbitalLogistic.mission.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.mission.application.ports.in.GetSpacecraftMissionsUseCase;
import org.orbitalLogistic.mission.application.ports.out.SpacecraftMissionRepository;
import org.orbitalLogistic.mission.domain.model.SpacecraftMission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetSpacecraftMissionsService implements GetSpacecraftMissionsUseCase {

    private final SpacecraftMissionRepository spacecraftMissionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SpacecraftMission> getAllSpacecraftMissions() {
        log.debug("Getting all spacecraft missions");
        return spacecraftMissionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpacecraftMission> getBySpacecraftId(Long spacecraftId) {
        log.debug("Finding spacecraft missions by spacecraft id: {}", spacecraftId);
        return spacecraftMissionRepository.findBySpacecraftId(spacecraftId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpacecraftMission> getByMissionId(Long missionId) {
        log.debug("Finding spacecraft missions by mission id: {}", missionId);
        return spacecraftMissionRepository.findByMissionId(missionId);
    }
}
