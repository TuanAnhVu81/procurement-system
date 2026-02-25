package com.anhvt.epms.procurement.service;

import com.anhvt.epms.procurement.dto.request.PurchaseOrderRequest;
import com.anhvt.epms.procurement.dto.response.PurchaseOrderResponse;
import com.anhvt.epms.procurement.dto.response.PurchaseOrderSummaryResponse;
import com.anhvt.epms.procurement.enums.POStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for Purchase Order operations
 * Defines business logic methods for PO management, workflow, and calculations
 */
public interface PurchaseOrderService {
    
    /**
     * Create a new purchase order with auto-generated PO number
     * Automatically calculates totalAmount, taxAmount, and grandTotal
     * 
     * @param request Purchase order request data
     * @return Created purchase order response
     */
    PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request);
    
    /**
     * Update an existing purchase order
     * Can only update if status is CREATED
     * Recalculates all totals
     * 
     * @param id Purchase order ID
     * @param request Updated purchase order data
     * @return Updated purchase order response
     */
    PurchaseOrderResponse updatePurchaseOrder(UUID id, PurchaseOrderRequest request);
    
    /**
     * Soft delete a purchase order
     * Sets isActive to false
     * 
     * @param id Purchase order ID
     */
    void deletePurchaseOrder(UUID id);
    
    /**
     * Get all purchase orders with pagination and sorting
     * 
     * @param pageable Pagination parameters
     * @return Page of purchase order summaries
     */
    Page<PurchaseOrderSummaryResponse> getAllPurchaseOrders(Pageable pageable);
    
    /**
     * Get purchase order by ID with full details
     * 
     * @param id Purchase order ID
     * @return Purchase order response with items
     */
    PurchaseOrderResponse getPurchaseOrderById(UUID id);
    
    /**
     * Get purchase orders by status
     * 
     * @param status PO status filter
     * @param pageable Pagination parameters
     * @return Page of purchase orders with given status
     */
    Page<PurchaseOrderSummaryResponse> getPurchaseOrdersByStatus(POStatus status, Pageable pageable);
    
    /**
     * Search purchase orders by PO number
     * 
     * @param keyword Search keyword
     * @param pageable Pagination parameters
     * @return Page of matching purchase orders
     */
    Page<PurchaseOrderSummaryResponse> searchPurchaseOrders(String keyword, Pageable pageable);
    
    /**
     * Submit purchase order for approval
     * Changes status from CREATED to PENDING
     * 
     * @param id Purchase order ID
     * @return Updated purchase order
     */
    PurchaseOrderResponse submitForApproval(UUID id);
    
    /**
     * Approve a purchase order
     * Changes status from PENDING to APPROVED
     * Records approver and approval date
     * 
     * @param id Purchase order ID
     * @return Approved purchase order
     */
    PurchaseOrderResponse approvePurchaseOrder(UUID id);
    
    /**
     * Reject a purchase order with reason
     * Changes status from PENDING to REJECTED
     * Records approver, rejection reason, and date
     * 
     * @param id Purchase order ID
     * @param rejectionReason Reason for rejection
     * @return Rejected purchase order
     */
    PurchaseOrderResponse rejectPurchaseOrder(UUID id, String rejectionReason);

    /**
     * Confirm goods received for an approved purchase order (Goods Receipt - GR)
     * Changes status from APPROVED to RECEIVED
     * Triggers MaterialStockService to update inventory for each line item
     *
     * @param id Purchase order ID
     * @return Updated purchase order with RECEIVED status
     */
    PurchaseOrderResponse receivePurchaseOrder(UUID id);
}
