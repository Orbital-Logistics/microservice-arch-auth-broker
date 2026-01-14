package org.orbitalLogistic.mission.infrastructure.adapters.in.rest.mapper;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.mission.application.ports.out.MissionAssignmentRepository;
import org.orbitalLogistic.mission.application.ports.out.SpacecraftServicePort;
import org.orbitalLogistic.mission.application.ports.out.UserServicePort;
import org.orbitalLogistic.mission.domain.model.Mission;
import org.orbitalLogistic.mission.infrastructure.adapters.in.rest.dto.response.MissionResponseDTO;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MissionDTOMapper {

    private final UserServicePort userServicePort;
    private final SpacecraftServicePort spacecraftServicePort;
    private final MissionAssignmentRepository missionAssignmentRepository;

    public MissionResponseDTO toResponseDTO(Mission mission) {
        String commanderName = userServicePort.getUserNameById(mission.getCommandingOfficerId());
        String spacecraftName = spacecraftServicePort.getSpacecraftNameById(mission.getSpacecraftId());
        Integer crewCount = mission.getId() != null 
            ? missionAssignmentRepository.countByMissionId(mission.getId()) 
            : 0;

        return new MissionResponseDTO(
            mission.getId(),
            mission.getMissionCode(),
            mission.getMissionName(),
            mission.getMissionType(),
            mission.getStatus(),
            mission.getPriority(),
            mission.getCommandingOfficerId(),
            commanderName,
            mission.getSpacecraftId(),
            spacecraftName,
            mission.getScheduledDeparture(),
            mission.getScheduledArrival(),
            crewCount
        );
    }
}
