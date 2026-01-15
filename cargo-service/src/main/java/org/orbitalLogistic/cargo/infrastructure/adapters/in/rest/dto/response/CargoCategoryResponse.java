package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargoCategoryResponse {
    
    private Long id;
    private String name;
    private Long parentCategoryId;
    private String parentCategoryName;
    private String description;
    private List<CargoCategoryResponse> children;
    private Integer level;
}
