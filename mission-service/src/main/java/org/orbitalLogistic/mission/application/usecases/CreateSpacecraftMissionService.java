package org.orbitalLogistic.mission.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.mission.application.ports.in.CreateSpacecraftMissionUseCase;
import org.orbitalLogistic.mission.application.ports.out.MissionRepository;
import org.orbitalLogistic.mission.application.ports.out.SpacecraftMissionRepository;
import org.orbitalLogistic.mission.application.ports.out.SpacecraftServicePort;
import org.orbitalLogistic.mission.domain.exception.MissionNotFoundException;
import org.orbitalLogistic.mission.domain.exception.MissionSpacecraftExistsException;
import org.orbitalLogistic.mission.domain.exception.SpacecraftServiceNotFound;
import org.orbitalLogistic.mission.domain.model.SpacecraftMission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateSpacecraftMissionService implements CreateSpacecraftMissionUseCase {

    private final SpacecraftMissionRepository spacecraftMissionRepository;
    private final MissionRepository missionRepository;
    private final SpacecraftServicePort spacecraftServicePort;

    @Override
    @Transactional
    public SpacecraftMission createSpacecraftMission(SpacecraftMission spacecraftMission) {
        log.debug("Creating spacecraft mission for spacecraft id: {} and mission id: {}", 
                  spacecraftMission.getSpacecraftId(), spacecraftMission.getMissionId());
        
        if (!missionRepository.existsById(spacecraftMission.getMissionId())) {
            throw new MissionNotFoundException("Mission not found with id: " + spacecraftMission.getMissionId());
        }

        if (spacecraftMissionRepository.existsBySpacecraftIdAndMissionId(
                spacecraftMission.getSpacecraftId(), spacecraftMission.getMissionId())) {
            throw new MissionSpacecraftExistsException("Such combination of mission id and spacecraft id is already exists!");
        }

        if (!spacecraftServicePort.spacecraftExists(spacecraftMission.getSpacecraftId())) {
            throw new SpacecraftServiceNotFound("Spacecraft not found with id: " + spacecraftMission.getSpacecraftId());
        }

        SpacecraftMission saved = spacecraftMissionRepository.save(spacecraftMission);
        log.info("Spacecraft mission created with id: {}", saved.getId());
        return saved;
    }
}
