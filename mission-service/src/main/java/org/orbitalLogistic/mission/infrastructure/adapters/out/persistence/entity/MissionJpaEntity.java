package org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.orbitalLogistic.mission.domain.model.enums.MissionPriority;
import org.orbitalLogistic.mission.domain.model.enums.MissionStatus;
import org.orbitalLogistic.mission.domain.model.enums.MissionType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("mission")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionJpaEntity {

    @Id
    private Long id;

    @Column("mission_code")
    @Size(max = 20, message = "Mission code must not exceed 20 characters")
    @NotBlank
    private String missionCode;

    @Column("mission_name")
    @Size(max = 200, message = "Mission name must not exceed 200 characters")
    @NotBlank
    private String missionName;

    @Column("mission_type")
    @NotNull
    private MissionType missionType;

    @Column("status")
    @Builder.Default
    @NotNull
    private MissionStatus status = MissionStatus.PLANNING;

    @Column("priority")
    @Builder.Default
    @NotNull
    private MissionPriority priority = MissionPriority.MEDIUM;

    @Column("commanding_officer_id")
    @NotNull(message = "Commanding officer is required")
    private Long commandingOfficerId;

    @Column("spacecraft_id")
    @NotNull(message = "Spacecraft is required")
    private Long spacecraftId;

    @Column("scheduled_departure")
    private LocalDateTime scheduledDeparture;

    @Column("scheduled_arrival")
    private LocalDateTime scheduledArrival;
}
