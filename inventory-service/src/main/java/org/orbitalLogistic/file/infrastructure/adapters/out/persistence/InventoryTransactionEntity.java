package org.orbitalLogistic.file.infrastructure.adapters.out.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("inventory_transaction")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionEntity {

    @Id
    private Long id;

    @Column("transaction_type")
    private String transactionType;

    @Column("cargo_id")
    private Long cargoId;

    @Column("quantity")
    private Integer quantity;

    @Column("from_storage_unit_id")
    private Long fromStorageUnitId;

    @Column("to_storage_unit_id")
    private Long toStorageUnitId;

    @Column("from_spacecraft_id")
    private Long fromSpacecraftId;

    @Column("to_spacecraft_id")
    private Long toSpacecraftId;

    @Column("performed_by_user_id")
    private Long performedByUserId;

    @Column("transaction_date")
    private LocalDateTime transactionDate;

    @Column("reason_code")
    private String reasonCode;

    @Column("notes")
    private String notes;
}
