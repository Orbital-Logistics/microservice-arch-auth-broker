package org.orbitalLogistic.mission.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.orbitalLogistic.mission.dto.request.MissionAssignmentRequestDTO;
import org.orbitalLogistic.mission.dto.response.MissionAssignmentResponseDTO;
import org.orbitalLogistic.mission.entities.MissionAssignment;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MissionAssignmentMapper {

    @Mapping(target = "missionName", source = "missionName")
    @Mapping(target = "userName", source = "userName")
    MissionAssignmentResponseDTO toResponseDTO(
        MissionAssignment assignment,
        String missionName,
        String userName
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedAt", expression = "java(java.time.LocalDateTime.now())")
    MissionAssignment toEntity(MissionAssignmentRequestDTO request);
}

