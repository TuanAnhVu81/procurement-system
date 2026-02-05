package com.anhvt.epms.procurement.repository;

import com.anhvt.epms.procurement.entity.PurchaseOrder;
import com.anhvt.epms.procurement.enums.POStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Purchase Order entity
 * Provides data access methods for purchase order operations
 */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    
    // Find by PO number (unique identifier)
    Optional<PurchaseOrder> findByPoNumber(String poNumber);
    
    // Find by status
    Page<PurchaseOrder> findByStatus(POStatus status, Pageable pageable);
    List<PurchaseOrder> findByStatus(POStatus status);
    
    // Find by vendor
    Page<PurchaseOrder> findByVendor_Id(UUID vendorId, Pageable pageable);
    
    // Find by creator (for employee to see their own POs) - using createdBy String field
    @Query("SELECT po FROM PurchaseOrder po WHERE po.createdBy = :username")
    Page<PurchaseOrder> findByCreatedBy(@Param("username") String username, Pageable pageable);
    
    // Search by PO number containing keyword
    Page<PurchaseOrder> findByPoNumberContainingIgnoreCase(String keyword, Pageable pageable);
    
    // Find by date range
    Page<PurchaseOrder> findByOrderDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    // Find active (not cancelled) purchase orders
    @Query("SELECT po FROM PurchaseOrder po WHERE po.status != 'CANCELLED'")
    Page<PurchaseOrder> findAllActive(Pageable pageable);
    
    // Count by status
    long countByStatus(POStatus status);
    
    // Custom query: Get latest PO number for auto-generation
    @Query("SELECT po.poNumber FROM PurchaseOrder po ORDER BY po.createdAt DESC LIMIT 1")
    Optional<String> findLatestPoNumber();
    
    // Analytics: Total purchase amount by vendor
    @Query("SELECT po.vendor.id, SUM(po.grandTotal) FROM PurchaseOrder po " +
           "WHERE po.status = 'APPROVED' AND po.status != 'CANCELLED' " +
           "GROUP BY po.vendor.id ORDER BY SUM(po.grandTotal) DESC")
    List<Object[]> getTotalPurchaseAmountByVendor();
    
    // Analytics: Count POs by status (excluding cancelled)
    @Query("SELECT po.status, COUNT(po) FROM PurchaseOrder po " +
           "WHERE po.status != 'CANCELLED' GROUP BY po.status")
    List<Object[]> countPurchaseOrdersByStatus();
    
    // Find POs pending approval (not cancelled)
    @Query("SELECT po FROM PurchaseOrder po WHERE po.status = 'PENDING' AND po.status != 'CANCELLED'")
    Page<PurchaseOrder> findPendingApprovalPOs(Pageable pageable);
    
    // Find by vendor and status
    Page<PurchaseOrder> findByVendor_IdAndStatus(UUID vendorId, POStatus status, Pageable pageable);
}
