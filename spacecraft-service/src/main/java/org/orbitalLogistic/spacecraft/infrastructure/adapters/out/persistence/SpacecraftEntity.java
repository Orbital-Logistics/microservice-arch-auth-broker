package org.orbitalLogistic.spacecraft.infrastructure.adapters.out.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("spacecraft")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpacecraftEntity {

    @Id
    private Long id;

    @Column("registry_code")
    private String registryCode;

    @Column("name")
    private String name;

    @Column("spacecraft_type_id")
    private Long spacecraftTypeId;

    @Column("mass_capacity")
    private BigDecimal massCapacity;

    @Column("volume_capacity")
    private BigDecimal volumeCapacity;

    @Column("status")
    private String status;

    @Column("current_location")
    private String currentLocation;
}

