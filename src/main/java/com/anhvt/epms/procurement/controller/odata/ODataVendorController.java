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
     */
    @GetMapping
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
        
        // Apply $filter if present
        Page<VendorResponse> vendorPage;
        if (filter != null && filter.contains("status eq")) {
            // Parse simple filter: "status eq 'ACTIVE'"
            String statusValue = filter.replaceAll(".*'(.*)'.*", "$1");
            Status status = Status.valueOf(statusValue.toUpperCase());
            vendorPage = vendorService.getVendorsByStatus(status, pageable);
        } else {
            vendorPage = vendorService.getAllVendors(pageable);
        }
        
        // Build OData response
        ODataCollectionResponse<VendorResponse> response = ODataCollectionResponse.<VendorResponse>builder()
                .context(ODATA_CONTEXT)
                .value(vendorPage.getContent())
                .build();
        
        // Add count if requested
        if (count) {
            response.setCount(vendorPage.getTotalElements());
        }
        
        // Add nextLink if there are more pages
        if (vendorPage.hasNext()) {
            int nextSkip = skip + top;
            String nextLink = String.format("/odata/Vendors?$top=%d&$skip=%d&$orderby=%s", 
                    top, nextSkip, orderby);
            response.setNextLink(nextLink);
        }
        
        log.info("OData response: returned {} vendors (total: {})", 
                vendorPage.getContent().size(), vendorPage.getTotalElements());
        
        return response;
    }
    
    /**
     * GET /odata/Vendors/{id} - Get single vendor by ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get vendor by ID",
        description = "Retrieve a single vendor by UUID"
    )
    public VendorResponse getVendorById(
            @Parameter(description = "Vendor UUID")
            @PathVariable UUID id) {
        
        log.info("OData: Get vendor by ID: {}", id);
        return vendorService.getVendorById(id);
    }
}
