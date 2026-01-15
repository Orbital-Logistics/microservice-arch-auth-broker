package org.orbitalLogistic.cargo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoCategory {
    private Long id;
    private String name;
    private Long parentCategoryId;
    private String description;
    private List<CargoCategory> children;
}
