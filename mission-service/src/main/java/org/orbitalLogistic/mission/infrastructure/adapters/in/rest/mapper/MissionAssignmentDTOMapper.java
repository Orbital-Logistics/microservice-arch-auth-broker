package org.orbitalLogistic.mission.infrastructure.adapters.in.rest.mapper;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.mission.application.ports.out.MissionRepository;
import org.orbitalLogistic.mission.application.ports.out.UserServicePort;
import org.orbitalLogistic.mission.domain.model.MissionAssignment;
import org.orbitalLogistic.mission.infrastructure.adapters.in.rest.dto.response.MissionAssignmentResponseDTO;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MissionAssignmentDTOMapper {

    private final UserServicePort userServicePort;
    private final MissionRepository missionRepository;

    public MissionAssignmentResponseDTO toResponseDTO(MissionAssignment assignment) {
        String userName = userServicePort.getUserNameById(assignment.getUserId());
        String missionName = missionRepository.findById(assignment.getMissionId())
                .map(mission -> mission.getMissionName())
                .orElse("Unknown");

        return new MissionAssignmentResponseDTO(
            assignment.getId(),
            assignment.getMissionId(),
            missionName,
            assignment.getUserId(),
            userName,
            assignment.getAssignedAt(),
            assignment.getAssignmentRole(),
            assignment.getResponsibilityZone()
        );
    }
}
