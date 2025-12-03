package org.orbitalLogistic.inventory.repositories;
import org.orbitalLogistic.inventory.entities.InventoryTransaction;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionRepository extends CrudRepository<InventoryTransaction, Long> {

    List<InventoryTransaction> findByCargoId(Long cargoId);

    @Query("""
        SELECT t.* FROM inventory_transaction t
        ORDER BY t.transaction_date DESC, t.id DESC
        LIMIT :limit OFFSET :offset
        """)
    List<InventoryTransaction> findAllPaginated(
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    @Query("""
        SELECT t.* FROM inventory_transaction t
        WHERE t.cargo_id = :cargoId
        ORDER BY t.transaction_date DESC, t.id DESC
        LIMIT :limit OFFSET :offset
        """)
    List<InventoryTransaction> findByCargoIdPaginated(
        @Param("cargoId") Long cargoId,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    @Query("SELECT COUNT(*) FROM inventory_transaction")
    long countAll();

    @Query("""
        SELECT COUNT(*) FROM inventory_transaction
        WHERE cargo_id = :cargoId
        """)
    long countByCargoId(@Param("cargoId") Long cargoId);
}