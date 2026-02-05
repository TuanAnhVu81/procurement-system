package com.anhvt.epms.procurement.controller;

import com.anhvt.epms.procurement.dto.request.PurchaseOrderApprovalRequest;
import com.anhvt.epms.procurement.dto.request.PurchaseOrderRequest;
import com.anhvt.epms.procurement.dto.response.ApiResponse;
import com.anhvt.epms.procurement.dto.response.PurchaseOrderResponse;
import com.anhvt.epms.procurement.dto.response.PurchaseOrderSummaryResponse;
import com.anhvt.epms.procurement.enums.POStatus;
import com.anhvt.epms.procurement.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for Purchase Order Management
 * Provides endpoints for CRUD operations and workflow management (submit, approve, reject)
 * Phase 1: WITHOUT authorization (@PreAuthorize) - will be added in Phase 4
 */
@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@Tag(name = "Purchase Order Management", description = "APIs for managing purchase orders and workflow")
public class PurchaseOrderController {
    
    private final PurchaseOrderService purchaseOrderService;
    
    /**
     * Create a new purchase order
     * POST /api/purchase-orders
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create new purchase order",
        description = "Create a new purchase order with auto-generated PO number and automatic calculation of totals"
    )
    public ApiResponse<PurchaseOrderResponse> createPurchaseOrder(
            @Valid @RequestBody PurchaseOrderRequest request) {
        
        PurchaseOrderResponse response = purchaseOrderService.createPurchaseOrder(request);
        
        return ApiResponse.<PurchaseOrderResponse>builder()
                .code(1000)
                .message("Purchase order created successfully with PO number: " + response.getPoNumber())
                .result(response)
                .build();
    }
    
    /**
     * Update an existing purchase order
     * PUT /api/purchase-orders/{id}
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update purchase order",
        description = "Update purchase order details. Can only update if status is CREATED"
    )
    public ApiResponse<PurchaseOrderResponse> updatePurchaseOrder(
            @PathVariable @Parameter(description = "Purchase Order ID") UUID id,
            @Valid @RequestBody PurchaseOrderRequest request) {
        
        PurchaseOrderResponse response = purchaseOrderService.updatePurchaseOrder(id, request);
        
        return ApiResponse.<PurchaseOrderResponse>builder()
                .code(1000)
                .message("Purchase order updated successfully")
                .result(response)
                .build();
    }
    
    /**
     * Soft delete a purchase order
     * DELETE /api/purchase-orders/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Delete purchase order",
        description = "Soft delete purchase order (sets isActive to false). Can only delete if status is CREATED"
    )
    public ApiResponse<Void> deletePurchaseOrder(
            @PathVariable @Parameter(description = "Purchase Order ID") UUID id) {
        
        purchaseOrderService.deletePurchaseOrder(id);
        
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Purchase order deleted successfully")
                .build();
    }
    
    /**
     * Get all purchase orders with pagination and sorting
     * GET /api/purchase-orders
     */
    @GetMapping
    @Operation(
        summary = "Get all purchase orders",
        description = "Retrieve all active purchase orders with pagination and sorting"
    )
    public Page<PurchaseOrderSummaryResponse> getAllPurchaseOrders(
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "Page size") int size,
            @RequestParam(defaultValue = "createdAt") @Parameter(description = "Sort field") String sortBy,
            @RequestParam(defaultValue = "DESC") @Parameter(description = "Sort direction") Sort.Direction direction) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return purchaseOrderService.getAllPurchaseOrders(pageable);
    }
    
    /**
     * Get purchase order by ID
     * GET /api/purchase-orders/{id}
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get purchase order by ID",
        description = "Retrieve detailed purchase order information including items and vendor details"
    )
    public ApiResponse<PurchaseOrderResponse> getPurchaseOrderById(
            @PathVariable @Parameter(description = "Purchase Order ID") UUID id) {
        
        PurchaseOrderResponse response = purchaseOrderService.getPurchaseOrderById(id);
        
        return ApiResponse.<PurchaseOrderResponse>builder()
                .code(1000)
                .message("Purchase order retrieved successfully")
                .result(response)
                .build();
    }
    
    /**
     * Get purchase orders by status
     * GET /api/purchase-orders/status/{status}
     */
    @GetMapping("/status/{status}")
    @Operation(
        summary = "Get purchase orders by status",
        description = "Filter purchase orders by status (CREATED, PENDING, APPROVED, REJECTED, CANCELLED)"
    )
    public Page<PurchaseOrderSummaryResponse> getPurchaseOrdersByStatus(
            @PathVariable @Parameter(description = "PO Status") POStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return purchaseOrderService.getPurchaseOrdersByStatus(status, pageable);
    }
    
    /**
     * Search purchase orders by PO number
     * GET /api/purchase-orders/search?keyword={keyword}
     */
    @GetMapping("/search")
    @Operation(
        summary = "Search purchase orders",
        description = "Search purchase orders by PO number (partial match, case insensitive)"
    )
    public Page<PurchaseOrderSummaryResponse> searchPurchaseOrders(
            @RequestParam @Parameter(description = "Search keyword") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return purchaseOrderService.searchPurchaseOrders(keyword, pageable);
    }
    
    /**
     * Submit purchase order for approval
     * POST /api/purchase-orders/{id}/submit
     * Changes status from CREATED to PENDING
     */
    @PostMapping("/{id}/submit")
    @Operation(
        summary = "Submit purchase order for approval",
        description = "Submit purchase order for approval. Changes status from CREATED to PENDING"
    )
    public ApiResponse<PurchaseOrderResponse> submitForApproval(
            @PathVariable @Parameter(description = "Purchase Order ID") UUID id) {
        
        PurchaseOrderResponse response = purchaseOrderService.submitForApproval(id);
        
        return ApiResponse.<PurchaseOrderResponse>builder()
                .code(1000)
                .message("Purchase order submitted for approval successfully")
                .result(response)
                .build();
    }
    
    /**
     * Approve purchase order
     * POST /api/purchase-orders/{id}/approve
     * Changes status from PENDING to APPROVED
     */
    @PostMapping("/{id}/approve")
    @Operation(
        summary = "Approve purchase order",
        description = "Approve a purchase order. Changes status from PENDING to APPROVED. Records approver and approval date"
    )
    public ApiResponse<PurchaseOrderResponse> approvePurchaseOrder(
            @PathVariable @Parameter(description = "Purchase Order ID") UUID id,
            @RequestBody(required = false) PurchaseOrderApprovalRequest request) {
        
        PurchaseOrderResponse response = purchaseOrderService.approvePurchaseOrder(id);
        
        return ApiResponse.<PurchaseOrderResponse>builder()
                .code(1000)
                .message("Purchase order approved successfully")
                .result(response)
                .build();
    }
    
    /**
     * Reject purchase order
     * POST /api/purchase-orders/{id}/reject
     * Changes status from PENDING to REJECTED
     */
    @PostMapping("/{id}/reject")
    @Operation(
        summary = "Reject purchase order",
        description = "Reject a purchase order with reason. Changes status from PENDING to REJECTED"
    )
    public ApiResponse<PurchaseOrderResponse> rejectPurchaseOrder(
            @PathVariable @Parameter(description = "Purchase Order ID") UUID id,
            @Valid @RequestBody PurchaseOrderApprovalRequest request) {
        
        String rejectionReason = request.getRejectionReason() != null 
                ? request.getRejectionReason() 
                : "No reason provided";
        
        PurchaseOrderResponse response = purchaseOrderService.rejectPurchaseOrder(id, rejectionReason);
        
        return ApiResponse.<PurchaseOrderResponse>builder()
                .code(1000)
                .message("Purchase order rejected successfully")
                .result(response)
                .build();
    }
}
