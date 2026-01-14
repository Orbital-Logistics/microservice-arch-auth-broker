package org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.orbitalLogistic.mission.domain.model.enums.AssignmentRole;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("mission_assignment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionAssignmentJpaEntity {

    @Id
    private Long id;

    @NotNull(message = "Mission is required")
    @Column("mission_id")
    private Long missionId;

    @NotNull(message = "User is required")
    @Column("user_id")
    private Long userId;

    @Builder.Default
    @Column("assigned_at")
    private LocalDateTime assignedAt = LocalDateTime.now();

    @NotNull
    @Column("assignment_role")
    private AssignmentRole assignmentRole;

    @Size(max = 100, message = "Responsibility zone must not exceed 100 characters")
    @Column("responsibility_zone")
    private String responsibilityZone;
}
