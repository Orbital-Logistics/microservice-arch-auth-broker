package org.scoooting.files.adapters.kafka.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReportDataDTO(
        String missionCode,
        String missionName,
        String missionType,
        String priority,
        Long commandingOfficerId,
        Long spacecraftId,
        LocalDateTime scheduledDeparture,
        LocalDateTime scheduledArrival
) {}
