package org.orbitalLogistic.mission.infrastructure.adapters.in.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.orbitalLogistic.mission.infrastructure.adapters.in.rest.dto.request.SpacecraftMissionRequestDTO;
import org.orbitalLogistic.mission.infrastructure.adapters.in.rest.dto.response.SpacecraftMissionResponseDTO;
import org.orbitalLogistic.mission.domain.model.SpacecraftMission;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SpacecraftMissionMapper {

    @Mapping(target = "spacecraftName", source = "spacecraftName")
    @Mapping(target = "missionName", source = "missionName")
    SpacecraftMissionResponseDTO toResponseDTO(
        SpacecraftMission spacecraftMission,
        String spacecraftName,
        String missionName
    );

    @Mapping(target = "id", ignore = true)
    SpacecraftMission toEntity(SpacecraftMissionRequestDTO request);
}

