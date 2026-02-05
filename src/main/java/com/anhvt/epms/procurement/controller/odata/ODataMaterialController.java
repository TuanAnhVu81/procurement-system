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
        
        // Apply $filter if present
        Page<MaterialResponse> materialPage;
        if (filter != null && filter.contains("isActive eq true")) {
            materialPage = materialService.getActiveMaterials(pageable);
        } else {
            materialPage = materialService.getAllMaterials(pageable);
        }
        
        // Build OData response
        ODataCollectionResponse<MaterialResponse> response = ODataCollectionResponse.<MaterialResponse>builder()
                .context(ODATA_CONTEXT)
                .value(materialPage.getContent())
                .build();
        
        // Add count if requested
        if (count) {
            response.setCount(materialPage.getTotalElements());
        }
        
        // Add nextLink if there are more pages
        if (materialPage.hasNext()) {
            int nextSkip = skip + top;
            String nextLink = String.format("/odata/Materials?$top=%d&$skip=%d&$orderby=%s", 
                    top, nextSkip, orderby);
            response.setNextLink(nextLink);
        }
        
        log.info("OData response: returned {} materials (total: {})", 
                materialPage.getContent().size(), materialPage.getTotalElements());
        
        return response;
    }
    
    /**
     * GET /odata/Materials/{id} - Get single material by ID
     * Required Role: ADMIN, EMPLOYEE, MANAGER
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'MANAGER')")
    @Operation(
        summary = "Get material by ID",
        description = "Retrieve a single material by UUID"
    )
    public MaterialResponse getMaterialById(
            @Parameter(description = "Material UUID")
            @PathVariable UUID id) {
        
        log.info("OData: Get material by ID: {}", id);
        return materialService.getMaterialById(id);
    }
}
