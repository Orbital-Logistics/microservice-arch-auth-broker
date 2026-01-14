package org.orbitalLogistic.mission.infrastructure.adapters.in.rest.mapper;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.mission.application.ports.out.MissionRepository;
import org.orbitalLogistic.mission.application.ports.out.SpacecraftServicePort;
import org.orbitalLogistic.mission.domain.model.SpacecraftMission;
import org.orbitalLogistic.mission.infrastructure.adapters.in.rest.dto.response.SpacecraftMissionResponseDTO;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpacecraftMissionDTOMapper {

    private final SpacecraftServicePort spacecraftServicePort;
    private final MissionRepository missionRepository;

    public SpacecraftMissionResponseDTO toResponseDTO(SpacecraftMission spacecraftMission) {
        String spacecraftName = spacecraftServicePort.getSpacecraftNameById(spacecraftMission.getSpacecraftId());
        String missionName = missionRepository.findById(spacecraftMission.getMissionId())
                .map(mission -> mission.getMissionName())
                .orElse("Unknown");

        return new SpacecraftMissionResponseDTO(
            spacecraftMission.getId(),
            spacecraftMission.getSpacecraftId(),
            spacecraftName,
            spacecraftMission.getMissionId(),
            missionName
        );
    }
}
