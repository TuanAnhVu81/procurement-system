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
        
        // ── Parse $filter expression ──────────────────────────────────────────
        // Supported syntax (as built by frontend odata.js):
        //   (1) No filter                  → fetch all
        //   (2) status eq 'APPROVED'        → filter by status only
        //   (3) contains(poNumber, 'kw')    → keyword search only
        //   (4) combined with 'and'         → status + keyword both applied
        Page<PurchaseOrderSummaryResponse> poPage;

        boolean hasStatus   = filter != null && filter.contains("status eq");
        boolean hasContains = filter != null && filter.contains("contains(");

        // Extract status value from filter string: status eq 'APPROVED' → APPROVED
        POStatus parsedStatus = null;
        if (hasStatus) {
            // Regex: grab first single-quoted token after "status eq"
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("status eq '([^']+)'")
                    .matcher(filter);
            if (m.find()) {
                parsedStatus = POStatus.valueOf(m.group(1).toUpperCase());
            }
        }

        // Extract keyword from contains(poNumber, 'keyword') or contains(vendorName, 'keyword')
        String parsedKeyword = null;
        if (hasContains) {
            // Regex: grab the keyword inside contains(..., 'keyword')
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("contains\\([^,]+,\\s*'([^']*)'\\)")
                    .matcher(filter);
            if (m.find()) {
                parsedKeyword = m.group(1);
            }
        }

        if (hasStatus && hasContains && parsedStatus != null && parsedKeyword != null) {
            // Case 4: Combined — status + keyword search
            // Delegate to searchPurchaseOrders then filter by status in memory
            // (production would use JPA Specification for full DB-side filtering)
            final POStatus finalStatus = parsedStatus;
            Page<PurchaseOrderSummaryResponse> searchPage =
                    purchaseOrderService.searchPurchaseOrders(parsedKeyword, pageable);
            java.util.List<PurchaseOrderSummaryResponse> filtered = searchPage.getContent()
                    .stream()
                    .filter(po -> finalStatus.name().equals(po.getStatus() != null ? po.getStatus().name() : null))
                    .collect(java.util.stream.Collectors.toList());
            poPage = new org.springframework.data.domain.PageImpl<>(filtered, pageable,
                    filtered.size());
            log.info("OData: Combined filter — status={}, keyword={}", parsedStatus, parsedKeyword);

        } else if (hasStatus && parsedStatus != null) {
            // Case 2: Status only
            poPage = purchaseOrderService.getPurchaseOrdersByStatus(parsedStatus, pageable);
            log.info("OData: Filter by status = {}", parsedStatus);

        } else if (hasContains && parsedKeyword != null) {
            // Case 3: Keyword only
            poPage = purchaseOrderService.searchPurchaseOrders(parsedKeyword, pageable);
            log.info("OData: Keyword search = '{}'", parsedKeyword);

        } else {
            // Case 1: No filter — fetch all
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
    

}
