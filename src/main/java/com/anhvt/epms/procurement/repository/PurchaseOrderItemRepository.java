package com.anhvt.epms.procurement.repository;

import com.anhvt.epms.procurement.entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Purchase Order Item entity
 * Provides data access methods for PO line items
 */
@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, UUID> {
    
    // Find all items for a specific purchase order
    List<PurchaseOrderItem> findByPurchaseOrder_Id(UUID purchaseOrderId);
    
    // Find items by material
    List<PurchaseOrderItem> findByMaterial_Id(UUID materialId);
    
    // Find items by purchase order and material
    List<PurchaseOrderItem> findByPurchaseOrder_IdAndMaterial_Id(UUID purchaseOrderId, UUID materialId);
    
    // Delete all items for a purchase order (for cascading soft delete)
    void deleteByPurchaseOrder_Id(UUID purchaseOrderId);
    
    // Analytics: Most purchased materials
    @Query("SELECT poi.material.id, poi.material.description, SUM(poi.quantity) " +
           "FROM PurchaseOrderItem poi " +
           "WHERE poi.purchaseOrder.status = 'APPROVED' " +
           "GROUP BY poi.material.id, poi.material.description " +
           "ORDER BY SUM(poi.quantity) DESC")
    List<Object[]> getMostPurchasedMaterials();
}
