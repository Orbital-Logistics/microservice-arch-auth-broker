package org.orbitalLogistic.mission.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.mission.application.ports.in.CreateMissionCommand;
import org.orbitalLogistic.mission.application.ports.in.CreateMissionUseCase;
import org.orbitalLogistic.mission.application.ports.out.MissionRepository;
import org.orbitalLogistic.mission.application.ports.out.SpacecraftServicePort;
import org.orbitalLogistic.mission.application.ports.out.UserServicePort;
import org.orbitalLogistic.mission.domain.exception.MissionAlreadyExistsException;
import org.orbitalLogistic.mission.domain.exception.SpacecraftServiceNotFound;
import org.orbitalLogistic.mission.domain.exception.UserServiceNotFound;
import org.orbitalLogistic.mission.domain.model.Mission;
import org.orbitalLogistic.mission.domain.model.enums.MissionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateMissionService implements CreateMissionUseCase {

    private final MissionRepository missionRepository;
    private final UserServicePort userServicePort;
    private final SpacecraftServicePort spacecraftServicePort;

    @Override
    @Transactional
    public Mission createMission(CreateMissionCommand command) {
        log.debug("Creating mission with code: {}", command.missionCode());
        
        if (missionRepository.existsByMissionCode(command.missionCode())) {
            throw new MissionAlreadyExistsException("Mission with code already exists: " + command.missionCode());
        }

        if (!userServicePort.userExists(command.commandingOfficerId())) {
            throw new UserServiceNotFound("Commanding officer not found with id: " + command.commandingOfficerId());
        }

        if (!spacecraftServicePort.spacecraftExists(command.spacecraftId())) {
            throw new SpacecraftServiceNotFound("Spacecraft not found with id: " + command.spacecraftId());
        }

        Mission mission = Mission.builder()
                .missionCode(command.missionCode())
                .missionName(command.missionName())
                .missionType(command.missionType())
                .status(MissionStatus.PLANNING)
                .priority(command.priority())
                .commandingOfficerId(command.commandingOfficerId())
                .spacecraftId(command.spacecraftId())
                .scheduledDeparture(command.scheduledDeparture())
                .scheduledArrival(command.scheduledArrival())
                .build();

        Mission saved = missionRepository.save(mission);
        log.info("Mission created with id: {}", saved.getId());
        return saved;
    }
}
