package org.orbitalLogistic.mission.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.mission.application.ports.in.GetMissionsUseCase;
import org.orbitalLogistic.mission.application.ports.out.MissionRepository;
import org.orbitalLogistic.mission.domain.model.Mission;
import org.orbitalLogistic.mission.domain.model.enums.MissionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetMissionsService implements GetMissionsUseCase {

    private final MissionRepository missionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Mission> getAllMissions() {
        log.debug("Getting all missions");
        return missionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mission> getMissionsByStatus(MissionStatus status) {
        log.debug("Finding missions by status: {}", status);
        return missionRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mission> getMissionsByCommandingOfficerId(Long commandingOfficerId) {
        log.debug("Finding missions by commanding officer id: {}", commandingOfficerId);
        return missionRepository.findByCommandingOfficerId(commandingOfficerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mission> getMissionsBySpacecraftId(Long spacecraftId) {
        log.debug("Finding missions by spacecraft id: {}", spacecraftId);
        return missionRepository.findBySpacecraftId(spacecraftId);
    }
}
