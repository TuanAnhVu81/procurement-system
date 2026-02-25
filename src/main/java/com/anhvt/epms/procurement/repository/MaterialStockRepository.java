package com.anhvt.epms.procurement.repository;

import com.anhvt.epms.procurement.entity.MaterialStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for MaterialStock entity
 * Provides queries for inventory management
 */
@Repository
public interface MaterialStockRepository extends JpaRepository<MaterialStock, UUID> {

    /**
     * Find the stock record by material ID
     * @param materialId the material UUID
     * @return Optional containing stock if found
     */
    Optional<MaterialStock> findByMaterialId(UUID materialId);

    /**
     * Find all materials whose stock has fallen below minimum level
     * Used to generate low-stock warnings
     * @return list of MaterialStock below minimum level
     */
    @Query("SELECT ms FROM MaterialStock ms WHERE ms.minimumStockLevel IS NOT NULL AND ms.quantityOnHand < ms.minimumStockLevel")
    List<MaterialStock> findAllBelowMinimumStockLevel();
}
