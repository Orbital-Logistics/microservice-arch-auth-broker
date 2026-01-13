package org.orbitalLogistic.spacecraft.infrastructure.adapters.out.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("spacecraft_type")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpacecraftTypeEntity {

    @Id
    private Long id;

    @Column("type_name")
    private String typeName;

    @Column("classification")
    private String classification;

    @Column("max_crew_capacity")
    private Integer maxCrewCapacity;
}

