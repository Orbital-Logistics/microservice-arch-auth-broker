package org.orbitalLogistic.file.adapters.kafka.dto;

import lombok.Builder;

import java.util.Set;

@Builder
public record UserReportDataDTO(
        String username,
        String password,
        String email,
        Set<Long> roleIds
) {}
