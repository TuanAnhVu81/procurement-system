package com.anhvt.epms.procurement.controller.odata;

import com.anhvt.epms.procurement.dto.response.ODataCollectionResponse;
import com.anhvt.epms.procurement.dto.response.VendorResponse;
import com.anhvt.epms.procurement.enums.Status;
import com.anhvt.epms.procurement.service.VendorService;
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
 * OData-style REST Controller for Vendors
 * Provides OData V4-compatible endpoints at /odata/Vendors
 * Supports: $top, $skip, $orderby, $filter, $count, $select
 */
@RestController
@RequestMapping("/odata/Vendors")
@Tag(name = "OData - Vendors", description = "OData V4-style API for vendor queries")
@RequiredArgsConstructor
@Slf4j
public class ODataVendorController {

    private final VendorService vendorService;
    private static final String ODATA_CONTEXT = "http://localhost:8080/odata/$metadata#Vendors";

    /**
     * GET /odata/Vendors - Get vendor collection
     * OData query options: $top, $skip, $orderby, $count, $filter
     * Required Role: ADMIN, EMPLOYEE, MANAGER
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'MANAGER')")
    @Operation(
        summary = "Get vendor collection (OData V4)",
        description = "Retrieve vendors with OData query options: $top, $skip, $orderby, $count, $filter"
    )
    public ODataCollectionResponse<VendorResponse> getVendors(
            @Parameter(description = "Maximum number of records to return")
            @RequestParam(name = "$top", defaultValue = "20") int top,
            
            @Parameter(description = "Number of records to skip")
            @RequestParam(name = "$skip", defaultValue = "0") int skip,
            
            @Parameter(description = "Sort field and direction (e.g., 'createdAt desc')")
            @RequestParam(name = "$orderby", defaultValue = "createdAt desc") String orderby,
            
            @Parameter(description = "Include total count in response")
            @RequestParam(name = "$count", defaultValue = "false") boolean count,
            
            @Parameter(description = "Filter expression (e.g., \"status eq 'ACTIVE'\")")
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
        // Supported syntax (same pattern as ODataPurchaseOrderController):
        //   (1) No filter                  → fetch all
        //   (2) status eq 'ACTIVE'          → filter by status
        //   (3) contains(name, 'kw')        → keyword search
        //   (4) combined with 'and'         → status + keyword
        Page<VendorResponse> vendorPage;

        boolean hasStatus   = filter != null && filter.contains("status eq");
        boolean hasContains = filter != null && filter.contains("contains(");

        // Extract status: status eq 'ACTIVE' → ACTIVE
        Status parsedStatus = null;
        if (hasStatus) {
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("status eq '([^']+)'")
                    .matcher(filter);
            if (m.find()) {
                parsedStatus = Status.valueOf(m.group(1).toUpperCase());
            }
        }

        // Extract keyword from contains(name, 'keyword') or contains(vendorCode, 'keyword')
        String parsedKeyword = null;
        if (hasContains) {
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("contains\\([^,]+,\\s*'([^']*)'\\)")
                    .matcher(filter);
            if (m.find()) {
                parsedKeyword = m.group(1);
            }
        }

        if (hasStatus && hasContains && parsedStatus != null && parsedKeyword != null) {
            // Case 4: Combined — search by keyword then filter by status in memory
            final Status finalStatus = parsedStatus;
            Page<VendorResponse> searched = vendorService.searchByKeyword(parsedKeyword, pageable);
            java.util.List<VendorResponse> filtered = searched.getContent().stream()
                    .filter(v -> finalStatus.name().equals(v.getStatus() != null ? v.getStatus().name() : null))
                    .collect(java.util.stream.Collectors.toList());
            vendorPage = new org.springframework.data.domain.PageImpl<>(filtered, pageable, filtered.size());
            log.info("OData Vendor: Combined status={}, keyword={}", parsedStatus, parsedKeyword);

        } else if (hasStatus && parsedStatus != null) {
            // Case 2: Status only
            vendorPage = vendorService.getVendorsByStatus(parsedStatus, pageable);
            log.info("OData Vendor: Filter by status={}", parsedStatus);

        } else if (hasContains && parsedKeyword != null) {
            // Case 3: Keyword only
            vendorPage = vendorService.searchByKeyword(parsedKeyword, pageable);
            log.info("OData Vendor: Keyword search='{}'", parsedKeyword);

        } else {
            // Case 1: No filter
            vendorPage = vendorService.getAllVendors(pageable);
        }

        // Build OData response envelope
        ODataCollectionResponse<VendorResponse> response = ODataCollectionResponse.<VendorResponse>builder()
                .context(ODATA_CONTEXT)
                .value(vendorPage.getContent())
                .build();

        // Inline count — returned when FE sends $count=true
        if (count) {
            response.setCount(vendorPage.getTotalElements());
        }

        // nextLink for cursor-based navigation (optional, useful for large datasets)
        if (vendorPage.hasNext()) {
            int nextSkip = skip + top;
            String nextLink = String.format("/odata/Vendors?$top=%d&$skip=%d&$orderby=%s", top, nextSkip, orderby);
            if (filter != null) nextLink += "&$filter=" + filter;
            response.setNextLink(nextLink);
        }

        log.info("OData Vendor response: {} vendors (total: {})", vendorPage.getContent().size(), vendorPage.getTotalElements());
        return response;
    }
}
