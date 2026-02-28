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
    
    // Find active POs for specific creator
    @Query("SELECT po FROM PurchaseOrder po WHERE po.createdBy = :username AND po.status != 'CANCELLED'")
    Page<PurchaseOrder> findActiveByCreatedBy(@Param("username") String username, Pageable pageable);
    
    // Count by status
    long countByStatus(POStatus status);
    
    // Custom query: Get latest PO number for auto-generation (by year prefix)
    // This ensures accurate sequence numbering when year changes
    @Query("SELECT po.poNumber FROM PurchaseOrder po " +
           "WHERE po.poNumber LIKE :prefix " +
           "ORDER BY po.poNumber DESC LIMIT 1")
    Optional<String> findLatestPoNumberByPrefix(@Param("prefix") String prefix);
    
    // Analytics: Total purchase amount by vendor (APPROVED or RECEIVED status only)
    // Using correct SQL GROUP BY on vendor ID to avoid redundant conditions
    @Query("SELECT po.vendor.id, SUM(po.grandTotal), po.currency FROM PurchaseOrder po " +
           "WHERE po.status IN ('APPROVED', 'RECEIVED') " +
           "GROUP BY po.vendor.id, po.currency " +
           "ORDER BY SUM(po.grandTotal) DESC")
    List<Object[]> getTotalPurchaseAmountByVendor();
    
    // Analytics: Count POs by status (excluding cancelled)
    // Returns [status, count, totalGrandTotal, currency] per group
    @Query("SELECT po.status, COUNT(po), SUM(po.grandTotal), po.currency " +
           "FROM PurchaseOrder po " +
           "WHERE po.status != 'CANCELLED' " +
           "GROUP BY po.status, po.currency")
    List<Object[]> countPurchaseOrdersByStatus();
    
    // Analytics: Monthly purchase trend query — aggregates POs within a date range
    // Returns [orderDate, count, currency, totalGrandTotal] per day-currency slice
    @Query("SELECT po.orderDate, COUNT(po), po.currency, SUM(po.grandTotal) " +
           "FROM PurchaseOrder po " +
           "WHERE po.orderDate BETWEEN :startDate AND :endDate " +
           "AND po.status != 'CANCELLED' " +
           "GROUP BY po.orderDate, po.currency " +
           "ORDER BY po.orderDate ASC")
    List<Object[]> findPurchaseTrendByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Find POs pending approval
    @Query("SELECT po FROM PurchaseOrder po WHERE po.status = 'PENDING'")
    Page<PurchaseOrder> findPendingApprovalPOs(Pageable pageable);

    // Find by vendor and status
    Page<PurchaseOrder> findByVendor_IdAndStatus(UUID vendorId, POStatus status, Pageable pageable);
}
