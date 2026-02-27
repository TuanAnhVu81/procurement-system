package com.anhvt.epms.procurement.controller.odata;

import com.anhvt.epms.procurement.dto.response.MaterialResponse;
import com.anhvt.epms.procurement.dto.response.ODataCollectionResponse;
import com.anhvt.epms.procurement.service.MaterialService;
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
 * OData-style REST Controller for Materials
 * Provides OData V4-compatible endpoints at /odata/Materials
 * Supports: $top, $skip, $orderby, $filter, $count, $select
 */
@RestController
@RequestMapping("/odata/Materials")
@Tag(name = "OData - Materials", description = "OData V4-style API for material queries")
@RequiredArgsConstructor
@Slf4j
public class ODataMaterialController {

    private final MaterialService materialService;
    private static final String ODATA_CONTEXT = "http://localhost:8080/odata/$metadata#Materials";

    /**
     * GET /odata/Materials - Get material collection
     * OData query options: $top, $skip, $orderby, $count, $filter
     * Required Role: ADMIN, EMPLOYEE, MANAGER
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'MANAGER')")
    @Operation(
        summary = "Get material collection (OData V4)",
        description = "Retrieve materials with OData query options: $top, $skip, $orderby, $count, $filter"
    )
    public ODataCollectionResponse<MaterialResponse> getMaterials(
            @Parameter(description = "Maximum number of records to return")
            @RequestParam(name = "$top", defaultValue = "20") int top,
            
            @Parameter(description = "Number of records to skip")
            @RequestParam(name = "$skip", defaultValue = "0") int skip,
            
            @Parameter(description = "Sort field and direction (e.g., 'createdAt desc')")
            @RequestParam(name = "$orderby", defaultValue = "createdAt desc") String orderby,
            
            @Parameter(description = "Include total count in response")
            @RequestParam(name = "$count", defaultValue = "false") boolean count,
            
            @Parameter(description = "Filter expression (e.g., \"isActive eq true\")")
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
        // Supported syntax for Materials:
        //   (1) No filter                         → fetch all
        //   (2) isActive eq true/false             → filter by active status
        //   (3) contains(description, 'kw')        → keyword search
        //   (4) combined with 'and'                → isActive + keyword
        Page<MaterialResponse> materialPage;

        boolean hasActive   = filter != null && filter.contains("isActive eq");
        boolean hasContains = filter != null && filter.contains("contains(");

        // Extract isActive value: isActive eq true → Boolean.TRUE
        Boolean parsedActive = null;
        if (hasActive) {
            parsedActive = filter.contains("isActive eq true");
        }

        // Extract keyword from contains(description, 'keyword') or contains(materialCode, 'keyword')
        String parsedKeyword = null;
        if (hasContains) {
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("contains\\([^,]+,\\s*'([^']*)'\\)")
                    .matcher(filter);
            if (m.find()) {
                parsedKeyword = m.group(1);
            }
        }

        if (hasActive && hasContains && parsedKeyword != null) {
            // Case 4: Combined — keyword search filtered by active status
            materialPage = materialService.searchByKeywordAndActive(parsedKeyword, parsedActive, pageable);
            log.info("OData Material: Combined isActive={}, keyword={}", parsedActive, parsedKeyword);

        } else if (hasActive) {
            // Case 2: isActive filter only
            materialPage = parsedActive
                    ? materialService.getActiveMaterials(pageable)
                    : materialService.getAllMaterials(pageable); // false = get all (including inactive)
            log.info("OData Material: Filter isActive={}", parsedActive);

        } else if (hasContains && parsedKeyword != null) {
            // Case 3: Keyword only
            materialPage = materialService.searchByKeyword(parsedKeyword, pageable);
            log.info("OData Material: Keyword search='{}'", parsedKeyword);

        } else {
            // Case 1: No filter
            materialPage = materialService.getAllMaterials(pageable);
        }

        // Build OData response envelope
        ODataCollectionResponse<MaterialResponse> response = ODataCollectionResponse.<MaterialResponse>builder()
                .context(ODATA_CONTEXT)
                .value(materialPage.getContent())
                .build();

        // Inline count — returned when FE sends $count=true
        if (count) {
            response.setCount(materialPage.getTotalElements());
        }

        // nextLink for cursor-based navigation
        if (materialPage.hasNext()) {
            int nextSkip = skip + top;
            String nextLink = String.format("/odata/Materials?$top=%d&$skip=%d&$orderby=%s", top, nextSkip, orderby);
            if (filter != null) nextLink += "&$filter=" + filter;
            response.setNextLink(nextLink);
        }

        log.info("OData Material response: {} materials (total: {})", materialPage.getContent().size(), materialPage.getTotalElements());
        return response;
    }
}
