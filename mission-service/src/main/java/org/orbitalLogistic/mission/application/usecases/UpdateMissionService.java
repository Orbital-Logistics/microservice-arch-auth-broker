package org.orbitalLogistic.mission.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.mission.application.ports.in.UpdateMissionCommand;
import org.orbitalLogistic.mission.application.ports.in.UpdateMissionUseCase;
import org.orbitalLogistic.mission.application.ports.out.MissionRepository;
import org.orbitalLogistic.mission.application.ports.out.SpacecraftServicePort;
import org.orbitalLogistic.mission.application.ports.out.UserServicePort;
import org.orbitalLogistic.mission.domain.exception.InvalidOperationException;
import org.orbitalLogistic.mission.domain.exception.MissionAlreadyExistsException;
import org.orbitalLogistic.mission.domain.exception.MissionNotFoundException;
import org.orbitalLogistic.mission.domain.model.Mission;
import org.orbitalLogistic.mission.domain.model.enums.MissionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateMissionService implements UpdateMissionUseCase {

    private final MissionRepository missionRepository;
    private final UserServicePort userServicePort;
    private final SpacecraftServicePort spacecraftServicePort;

    @Override
    @Transactional
    public Mission updateMission(UpdateMissionCommand command) {
        log.debug("Updating mission with id: {}", command.id());
        
        Mission existing = missionRepository.findById(command.id())
                .orElseThrow(() -> new MissionNotFoundException("Mission not found with id: " + command.id()));

        if (!existing.getMissionCode().equals(command.missionCode()) &&
                missionRepository.existsByMissionCode(command.missionCode())) {
            throw new MissionAlreadyExistsException("Mission with code already exists: " + command.missionCode());
        }

        if (!userServicePort.userExists(command.commandingOfficerId())) {
            throw new InvalidOperationException("Commanding officer not found with id: " + command.commandingOfficerId());
        }

        if (!spacecraftServicePort.spacecraftExists(command.spacecraftId())) {
            throw new InvalidOperationException("Spacecraft not found with id: " + command.spacecraftId());
        }

        existing.setMissionCode(command.missionCode());
        existing.setMissionName(command.missionName());
        existing.setMissionType(command.missionType());
        existing.setPriority(command.priority());
        existing.setCommandingOfficerId(command.commandingOfficerId());
        existing.setSpacecraftId(command.spacecraftId());
        existing.setScheduledDeparture(command.scheduledDeparture());
        existing.setScheduledArrival(command.scheduledArrival());

        Mission updated = missionRepository.save(existing);
        log.info("Mission updated with id: {}", updated.getId());
        return updated;
    }

    @Override
    @Transactional
    public Mission updateMissionStatus(Long id, MissionStatus status) {
        log.debug("Updating mission status for id: {} to: {}", id, status);
        
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new MissionNotFoundException("Mission not found with id: " + id));

        mission.setStatus(status);
        Mission updated = missionRepository.save(mission);
        log.info("Mission status updated for id: {}", updated.getId());
        return updated;
    }
}
