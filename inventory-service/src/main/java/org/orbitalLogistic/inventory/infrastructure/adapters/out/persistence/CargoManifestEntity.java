package org.orbitalLogistic.inventory.infrastructure.adapters.out.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("cargo_manifest")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoManifestEntity {

    @Id
    private Long id;

    @Column("spacecraft_id")
    private Long spacecraftId;

    @Column("cargo_id")
    private Long cargoId;

    @Column("storage_unit_id")
    private Long storageUnitId;

    @Column("quantity")
    private Integer quantity;

    @Column("loaded_at")
    private LocalDateTime loadedAt;

    @Column("unloaded_at")
    private LocalDateTime unloadedAt;

    @Column("loaded_by_user_id")
    private Long loadedByUserId;

    @Column("unloaded_by_user_id")
    private Long unloadedByUserId;

    @Column("manifest_status")
    private String manifestStatus;

    @Column("priority")
    private String priority;
}
