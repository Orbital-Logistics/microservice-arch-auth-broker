package org.orbitalLogistic.file.adapters.kafka.dto;

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
