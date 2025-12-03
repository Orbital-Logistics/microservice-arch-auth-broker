package org.orbitalLogistic.mission.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.orbitalLogistic.mission.dto.request.MissionRequestDTO;
import org.orbitalLogistic.mission.dto.response.MissionResponseDTO;
import org.orbitalLogistic.mission.entities.Mission;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MissionMapper {
    @Mapping(target = "commanderName", source = "commanderName")
    @Mapping(target = "spacecraftName", source = "spacecraftName")
    @Mapping(target = "assignedCrewCount", source = "assignedCrewCount")
    MissionResponseDTO toResponseDTO(
            Mission mission,
            String commanderName,
            String spacecraftName,
            Integer assignedCrewCount
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PLANNING")
    Mission toEntity(MissionRequestDTO request);
}




