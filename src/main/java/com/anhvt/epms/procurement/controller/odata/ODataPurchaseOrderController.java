package com.anhvt.epms.procurement.controller.odata;

import com.anhvt.epms.procurement.dto.response.ODataCollectionResponse;
import com.anhvt.epms.procurement.dto.response.PurchaseOrderResponse;
import com.anhvt.epms.procurement.dto.response.PurchaseOrderSummaryResponse;
import com.anhvt.epms.procurement.enums.POStatus;
import com.anhvt.epms.procurement.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * OData-style REST Controller for Purchase Orders
 * Provides OData V4-compatible endpoints at /odata/PurchaseOrders
 * Supports: $top, $skip, $orderby, $filter, $count, $select
 * 
 * Example queries:
 * - /odata/PurchaseOrders?$top=10&$skip=0&$count=true
 * - /odata/PurchaseOrders?$filter=status eq 'APPROVED'
 * - /odata/PurchaseOrders?$orderby=orderDate desc
 */
@RestController
@RequestMapping("/odata/PurchaseOrders")
@Tag(name = "OData - Purchase Orders", description = "OData V4-style API for purchase order queries")
@RequiredArgsConstructor
@Slf4j
public class ODataPurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private static final String ODATA_CONTEXT = "http://localhost:8080/procurement/odata/$metadata#PurchaseOrders";

    /**
     * GET /odata/PurchaseOrders - Get purchase order collection
     * OData query options: $top, $skip, $orderby, $count, $filter
     * 
     * Supported filters:
     * - status eq 'CREATED' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'
     * - vendorId eq '{uuid}'
     * 
     * Required Role: ADMIN, EMPLOYEE, MANAGER
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'MANAGER')")
    @Operation(
        summary = "Get purchase order collection (OData V4)",
        description = "Retrieve purchase orders with OData query options: $top, $skip, $orderby, $count, $filter"
    )
    public ODataCollectionResponse<PurchaseOrderSummaryResponse> getPurchaseOrders(
            @Parameter(description = "Maximum number of records to return")
            @RequestParam(name = "$top", defaultValue = "20") int top,
            
            @Parameter(description = "Number of records to skip")
            @RequestParam(name = "$skip", defaultValue = "0") int skip,
            
            @Parameter(description = "Sort field and direction (e.g., 'orderDate desc')")
            @RequestParam(name = "$orderby", defaultValue = "createdAt desc") String orderby,
            
            @Parameter(description = "Include total count in response")
            @RequestParam(name = "$count", defaultValue = "false") boolean count,
            
            @Parameter(description = "Filter expression (e.g., \"status eq 'APPROVED'\")")
            @RequestParam(name = "$filter", required = false) String filter) {
        
        log.info("OData query: $top={}, $skip={}, $orderby={}, $count={}, $filter={}", 
                top, skip, orderby, count, filter);
        
        // Parse $orderby (format: "fieldName desc" or "fieldName asc" or "fieldName")
        String[] orderParts = orderby.trim().split("\\s+");
        String sortField = orderParts[0];
        Sort.Direction direction = (orderParts.length > 1 && orderParts[1].equalsIgnoreCase("desc")) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
        
        // Create Pageable
        int page = skip / top;
        Pageable pageable = PageRequest.of(page, top, Sort.by(direction, sortField));
        
        // Apply $filter if present
        Page<PurchaseOrderSummaryResponse> poPage;
        if (filter != null && filter.contains("status eq")) {
            // Parse filter: "status eq 'APPROVED'"
            String statusValue = filter.replaceAll(".*'(.*)'.*", "$1");
            POStatus status = POStatus.valueOf(statusValue.toUpperCase());
            poPage = purchaseOrderService.getPurchaseOrdersByStatus(status, pageable);
            log.info("OData: Filtering by status = {}", status);
        } else {
            poPage = purchaseOrderService.getAllPurchaseOrders(pageable);
        }
        
        // Build OData response
        ODataCollectionResponse<PurchaseOrderSummaryResponse> response = ODataCollectionResponse.<PurchaseOrderSummaryResponse>builder()
                .context(ODATA_CONTEXT)
                .value(poPage.getContent())
                .build();
        
        // Add count if requested
        if (count) {
            response.setCount(poPage.getTotalElements());
        }
        
        // Add nextLink if there are more pages
        if (poPage.hasNext()) {
            int nextSkip = skip + top;
            String nextLink = String.format("/procurement/odata/PurchaseOrders?$top=%d&$skip=%d&$orderby=%s", 
                    top, nextSkip, orderby);
            if (filter != null) {
                nextLink += "&$filter=" + filter;
            }
            response.setNextLink(nextLink);
        }
        
        log.info("OData response: returned {} purchase orders (total: {})", 
                poPage.getContent().size(), poPage.getTotalElements());
        
        return response;
    }
    
    /**
     * GET /odata/PurchaseOrders/{id} - Get single purchase order by ID
     * Returns full details including items, vendor, and approver information
     * Required Role: ADMIN, EMPLOYEE, MANAGER
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'MANAGER')")
    @Operation(
        summary = "Get purchase order by ID",
        description = "Retrieve a single purchase order with full details by UUID"
    )
    public PurchaseOrderResponse getPurchaseOrderById(
            @Parameter(description = "Purchase Order UUID")
            @PathVariable UUID id) {
        
        log.info("OData: Get purchase order by ID: {}", id);
        return purchaseOrderService.getPurchaseOrderById(id);
    }
    
    /**
     * GET /odata/PurchaseOrders/$count - Get total count of purchase orders
     * OData standard endpoint for getting count only
     * Required Role: ADMIN, EMPLOYEE, MANAGER
     */
    @GetMapping("/$count")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'MANAGER')")
    @Operation(
        summary = "Get total count of purchase orders",
        description = "Returns the total number of purchase orders (OData $count endpoint)"
    )
    public Long getPurchaseOrderCount(
            @Parameter(description = "Filter expression")
            @RequestParam(name = "$filter", required = false) String filter) {
        
        log.info("OData: Get purchase order count, filter={}", filter);
        
        // For now, return count from getAllPurchaseOrders
        // In production, you might want a dedicated count query
        Pageable pageable = PageRequest.of(0, 1);
        
        Page<PurchaseOrderSummaryResponse> poPage;
        if (filter != null && filter.contains("status eq")) {
            String statusValue = filter.replaceAll(".*'(.*)'.*", "$1");
            POStatus status = POStatus.valueOf(statusValue.toUpperCase());
            poPage = purchaseOrderService.getPurchaseOrdersByStatus(status, pageable);
        } else {
            poPage = purchaseOrderService.getAllPurchaseOrders(pageable);
        }
        
        return poPage.getTotalElements();
    }
}
