package org.orbitalLogistic.mission.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpacecraftMission {
    private Long id;
    private Long spacecraftId;
    private Long missionId;
}
