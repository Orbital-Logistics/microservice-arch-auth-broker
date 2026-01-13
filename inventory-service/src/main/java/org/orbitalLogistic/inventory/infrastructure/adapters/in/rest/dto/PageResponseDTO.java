package org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.dto;

import java.util.List;

public record PageResponseDTO<T>(
        List<T> content,
        int currentPage,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
