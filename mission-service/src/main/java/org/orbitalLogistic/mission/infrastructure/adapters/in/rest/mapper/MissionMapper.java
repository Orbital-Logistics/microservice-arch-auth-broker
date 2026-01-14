package org.orbitalLogistic.mission.infrastructure.adapters.in.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.orbitalLogistic.mission.infrastructure.adapters.in.rest.dto.request.MissionRequestDTO;
import org.orbitalLogistic.mission.domain.model.Mission;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MissionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PLANNING")
    Mission toEntity(MissionRequestDTO request);
}




