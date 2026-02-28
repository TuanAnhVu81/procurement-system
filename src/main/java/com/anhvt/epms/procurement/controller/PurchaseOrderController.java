package com.anhvt.epms.procurement.controller;

import com.anhvt.epms.procurement.dto.request.PurchaseOrderApprovalRequest;
import com.anhvt.epms.procurement.dto.request.PurchaseOrderRequest;
import com.anhvt.epms.procurement.dto.response.ApiResponse;
import com.anhvt.epms.procurement.dto.response.PurchaseOrderResponse;
import com.anhvt.epms.procurement.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for Purchase Order Management
 * Provides endpoints for CRUD operations and workflow management
 * (submit, approve, reject, receive).
 *
 * Authorization Rules:
 * - EMPLOYEE: Create, Update (creator only), Delete (creator only),
 *             Submit (creator only), Receive (goods receipt)
 * - MANAGER:  Approve, Reject
 * - ADMIN:    Read detail by ID
 *
 * NOTE: GET List endpoints (getAllPurchaseOrders, getPurchaseOrdersByStatus,
 * searchPurchaseOrders) have been REMOVED.
 * Use OData V4 endpoint instead: GET /odata/PurchaseOrders
 *   - Filter by status:   $filter=status eq 'PENDING'
 *   - Search by PO code:  $filter=contains(poNumber,'kw')
 *   - Combined:           $filter=status eq 'APPROVED' and contains(vendorName,'FPT')
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
     * Required Role: EMPLOYEE
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(
        summary = "Create new purchase order",
        description = "Create a new purchase order with auto-generated PO number and automatic calculation of totals"
    )
    public ApiResponse<PurchaseOrderResponse> createPurchaseOrder(
            @Valid @RequestBody PurchaseOrderRequest request) {

        PurchaseOrderResponse response = purchaseOrderService.createPurchaseOrder(request);

        return ApiResponse.<PurchaseOrderResponse>builder()
                .message("Purchase order created successfully with PO number: " + response.getPoNumber())
                .result(response)
                .build();
    }

    /**
     * Update an existing purchase order
     * PUT /api/purchase-orders/{id}
     * Required Role: EMPLOYEE (creator only — validated at service layer)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(
        summary = "Update purchase order",
        description = "Update purchase order details. Can only update if status is CREATED and you are the creator"
    )
    public ApiResponse<PurchaseOrderResponse> updatePurchaseOrder(
            @PathVariable @Parameter(description = "Purchase Order ID") UUID id,
            @Valid @RequestBody PurchaseOrderRequest request) {

        PurchaseOrderResponse response = purchaseOrderService.updatePurchaseOrder(id, request);

        return ApiResponse.<PurchaseOrderResponse>builder()
                .message("Purchase order updated successfully")
                .result(response)
                .build();
    }

    /**
     * Soft delete a purchase order
     * DELETE /api/purchase-orders/{id}
     * Required Role: EMPLOYEE (creator only — validated at service layer)
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(
        summary = "Delete purchase order",
        description = "Soft delete purchase order. Can only delete if status is CREATED and you are the creator"
    )
    public ApiResponse<Void> deletePurchaseOrder(
            @PathVariable @Parameter(description = "Purchase Order ID") UUID id) {

        purchaseOrderService.deletePurchaseOrder(id);

        return ApiResponse.<Void>builder()
                .message("Purchase order deleted successfully")
                .build();
    }

    /**
     * Get purchase order by ID (full detail with line items)
     * GET /api/purchase-orders/{id}
     * Required Role: ADMIN, EMPLOYEE, MANAGER
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'MANAGER')")
    @Operation(
        summary = "Get purchase order by ID",
        description = "Retrieve detailed purchase order information including items and vendor details"
    )
    public ApiResponse<PurchaseOrderResponse> getPurchaseOrderById(
            @PathVariable @Parameter(description = "Purchase Order ID") UUID id) {

        PurchaseOrderResponse response = purchaseOrderService.getPurchaseOrderById(id);

        return ApiResponse.<PurchaseOrderResponse>builder()
                .message("Purchase order retrieved successfully")
                .result(response)
                .build();
    }

    /**
     * Submit purchase order for approval
     * POST /api/purchase-orders/{id}/submit
     * Changes status: CREATED → PENDING
     * Required Role: EMPLOYEE (creator only — validated at service layer)
     */
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(
        summary = "Submit purchase order for approval",
        description = "Submit purchase order for approval. Changes status from CREATED to PENDING. Only creator can submit"
    )
    public ApiResponse<PurchaseOrderResponse> submitForApproval(
            @PathVariable @Parameter(description = "Purchase Order ID") UUID id) {

        PurchaseOrderResponse response = purchaseOrderService.submitForApproval(id);

        return ApiResponse.<PurchaseOrderResponse>builder()
                .message("Purchase order submitted for approval successfully")
                .result(response)
                .build();
    }

    /**
     * Approve purchase order
     * POST /api/purchase-orders/{id}/approve
     * Changes status: PENDING → APPROVED
     * Required Role: MANAGER
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
        summary = "Approve purchase order",
        description = "Approve a purchase order. Changes status from PENDING to APPROVED. Records approver and approval date"
    )
    public ApiResponse<PurchaseOrderResponse> approvePurchaseOrder(
            @PathVariable @Parameter(description = "Purchase Order ID") UUID id,
            @RequestBody(required = false) PurchaseOrderApprovalRequest request) {

        PurchaseOrderResponse response = purchaseOrderService.approvePurchaseOrder(id);

        return ApiResponse.<PurchaseOrderResponse>builder()
                .message("Purchase order approved successfully")
                .result(response)
                .build();
    }

    /**
     * Reject purchase order
     * POST /api/purchase-orders/{id}/reject
     * Changes status: PENDING → REJECTED
     * Required Role: MANAGER
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('MANAGER')")
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
                .message("Purchase order rejected successfully")
                .result(response)
                .build();
    }

    /**
     * Confirm Goods Receipt (GR) for an approved purchase order
     * POST /api/purchase-orders/{id}/receive
     * Changes status: APPROVED → RECEIVED
     * Triggers MaterialStock update for all line items
     * Required Role: EMPLOYEE
     */
    @PostMapping("/{id}/receive")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(
        summary = "Confirm goods received",
        description = "Warehouse staff confirms that goods have physically arrived. " +
                      "Changes status from APPROVED to RECEIVED and automatically updates MaterialStock " +
                      "(quantityOnHand += received quantity). Emits low-stock warning if stock falls below minimum."
    )
    public ApiResponse<PurchaseOrderResponse> receivePurchaseOrder(
            @PathVariable @Parameter(description = "Purchase Order ID") UUID id) {

        PurchaseOrderResponse response = purchaseOrderService.receivePurchaseOrder(id);

        return ApiResponse.<PurchaseOrderResponse>builder()
                .message("Goods receipt confirmed. Stock has been updated for PO: " + response.getPoNumber())
                .result(response)
                .build();
    }
}
