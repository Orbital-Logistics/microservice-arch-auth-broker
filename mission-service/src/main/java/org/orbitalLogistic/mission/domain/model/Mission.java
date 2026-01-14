package org.orbitalLogistic.mission.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.orbitalLogistic.mission.domain.model.enums.MissionPriority;
import org.orbitalLogistic.mission.domain.model.enums.MissionStatus;
import org.orbitalLogistic.mission.domain.model.enums.MissionType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mission {
    private Long id;
    private String missionCode;
    private String missionName;
    private MissionType missionType;
    private MissionStatus status;
    private MissionPriority priority;
    private Long commandingOfficerId;
    private Long spacecraftId;
    private LocalDateTime scheduledDeparture;
    private LocalDateTime scheduledArrival;
}
