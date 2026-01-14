package org.orbitalLogistic.mission.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.orbitalLogistic.mission.domain.model.enums.AssignmentRole;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionAssignment {
    private Long id;
    private Long missionId;
    private Long userId;
    private LocalDateTime assignedAt;
    private AssignmentRole assignmentRole;
    private String responsibilityZone;
}
