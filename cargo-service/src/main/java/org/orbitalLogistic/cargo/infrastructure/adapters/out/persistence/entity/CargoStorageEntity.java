package org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("cargo_storage")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoStorageEntity {

    @Id
    private Long id;

    @Column("storage_unit_id")
    private Long storageUnitId;

    @Column("cargo_id")
    private Long cargoId;

    @Column("quantity")
    private Integer quantity;

    @Builder.Default
    @Column("stored_at")
    private LocalDateTime storedAt = LocalDateTime.now();

    @Column("last_inventory_check")
    private LocalDateTime lastInventoryCheck;

    @Column("last_checked_by_user_id")
    private Long lastCheckedByUserId;
}
