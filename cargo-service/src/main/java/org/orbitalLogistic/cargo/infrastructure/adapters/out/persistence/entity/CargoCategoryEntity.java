package org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Table("cargo_category")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoCategoryEntity {

    @Id
    private Long id;

    private String name;

    @Column("parent_category_id")
    private Long parentCategoryId;

    private String description;

    @Transient
    private List<CargoCategoryEntity> children;
}
