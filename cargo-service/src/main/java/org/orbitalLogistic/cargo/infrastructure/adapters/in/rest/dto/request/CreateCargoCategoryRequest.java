package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCargoCategoryRequest {
    
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;
    
    private Long parentCategoryId;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
