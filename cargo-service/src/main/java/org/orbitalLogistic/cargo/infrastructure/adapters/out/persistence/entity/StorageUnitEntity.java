package org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("storage_unit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageUnitEntity {

    @Id
    private Long id;

    @Column("unit_code")
    private String unitCode;

    private String location;

    @Column("storage_type")
    private String storageType;

    @Column("total_mass_capacity")
    private BigDecimal totalMassCapacity;

    @Column("total_volume_capacity")
    private BigDecimal totalVolumeCapacity;

    @Column("current_mass")
    private BigDecimal currentMass;

    @Column("current_volume")
    private BigDecimal currentVolume;
}
