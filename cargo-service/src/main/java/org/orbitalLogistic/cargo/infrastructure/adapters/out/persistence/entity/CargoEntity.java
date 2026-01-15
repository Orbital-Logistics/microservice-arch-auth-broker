package org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("cargo")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoEntity {

    @Id
    private Long id;

    private String name;

    @Column("cargo_category_id")
    private Long cargoCategoryId;

    @Column("mass_per_unit")
    private BigDecimal massPerUnit;

    @Column("volume_per_unit")
    private BigDecimal volumePerUnit;

    @Column("cargo_type")
    private String cargoType;

    @Column("hazard_level")
    private String hazardLevel;

    @Builder.Default
    @Column("is_active")
    private Boolean isActive = true;
}
