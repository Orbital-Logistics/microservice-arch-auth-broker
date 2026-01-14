package org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("spacecraft_mission")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpacecraftMissionJpaEntity {

    @Id
    private Long id;

    @NotNull(message = "Spacecraft is required")
    @Column("spacecraft_id")
    private Long spacecraftId;

    @NotNull(message = "Mission is required")
    @Column("mission_id")
    private Long missionId;
}
