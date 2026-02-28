package com.anhvt.epms.procurement.controller;

import com.anhvt.epms.procurement.dto.request.MaterialRequest;
import com.anhvt.epms.procurement.dto.response.ApiResponse;
import com.anhvt.epms.procurement.dto.response.MaterialResponse;
import com.anhvt.epms.procurement.dto.response.MaterialStockResponse;
import com.anhvt.epms.procurement.service.MaterialService;
import com.anhvt.epms.procurement.service.MaterialStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for material management endpoints
 * Provides CRUD operations and material-specific features.
 *
 * Authorization Rules:
 * - ADMIN: Full access (Create, Read, Update, Delete)
 * - EMPLOYEE: Read-only access
 * - MANAGER: Read-only access
 *
 * NOTE: GET List endpoints (getAllMaterials, getActiveMaterials, searchByCode,
 * getMaterialsByUnit, getMaterialsByCategory) have been REMOVED.
 * Use OData V4 endpoint instead: GET /odata/Materials
 *   - Filter active:    $filter=isActive eq true
 *   - Search by name:   $filter=contains(description,'kw')
 *   - Filter by type:   $filter=materialType eq 'RAW'
 */
@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
@Tag(name = "Material Management", description = "APIs for managing materials (items/products)")
public class MaterialController {

    private final MaterialService materialService;
    // Injected to expose stock endpoints under the same base URL
    private final MaterialStockService materialStockService;

    /**
     * Create a new material
     * POST /api/materials
     * Required Role: ADMIN
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new material", description = "Create a new material with auto-generated material code")
    public ApiResponse<MaterialResponse> createMaterial(@Valid @RequestBody MaterialRequest request) {
        MaterialResponse response = materialService.createMaterial(request);

        return ApiResponse.<MaterialResponse>builder()
                .message("Material created successfully")
                .result(response)
                .build();
    }

    /**
     * Get material by ID
     * GET /api/materials/{id}
     * Required Role: ADMIN, EMPLOYEE, MANAGER
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'MANAGER')")
    @Operation(summary = "Get material by ID", description = "Retrieve a specific material by its ID")
    public ApiResponse<MaterialResponse> getMaterialById(@PathVariable UUID id) {
        MaterialResponse response = materialService.getMaterialById(id);

        return ApiResponse.<MaterialResponse>builder()
                .message("Material retrieved successfully")
                .result(response)
                .build();
    }

    /**
     * Update material
     * PUT /api/materials/{id}
     * Required Role: ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update material", description = "Update material information (material code cannot be changed)")
    public ApiResponse<MaterialResponse> updateMaterial(
            @PathVariable UUID id,
            @Valid @RequestBody MaterialRequest request) {
        MaterialResponse response = materialService.updateMaterial(id, request);

        return ApiResponse.<MaterialResponse>builder()
                .message("Material updated successfully")
                .result(response)
                .build();
    }

    /**
     * Delete material (soft delete)
     * DELETE /api/materials/{id}
     * Required Role: ADMIN
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete material", description = "Soft delete material (sets isActive to false, preserves data)")
    public ApiResponse<Void> deleteMaterial(@PathVariable UUID id) {
        materialService.deleteMaterial(id);

        return ApiResponse.<Void>builder()
                .message("Material deleted successfully")
                .build();
    }

    /**
     * Get stock level for a specific material
     * GET /api/materials/{id}/stock
     * Required Role: ADMIN, EMPLOYEE, MANAGER
     */
    @GetMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'MANAGER')")
    @Operation(summary = "Get material stock level", description = "Get current inventory level for a specific material")
    public ApiResponse<MaterialStockResponse> getMaterialStock(@PathVariable UUID id) {
        MaterialStockResponse response = materialStockService.getStockByMaterialId(id);
        return ApiResponse.<MaterialStockResponse>builder()
                .message("Stock retrieved successfully")
                .result(response)
                .build();
    }

    /**
     * Get all materials below minimum stock level (Low-stock warning)
     * GET /api/materials/stock/low
     * Required Role: ADMIN, MANAGER
     */
    @GetMapping("/stock/low")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get low-stock materials", description = "List all materials with quantity below their minimum stock level")
    public ApiResponse<List<MaterialStockResponse>> getLowStockMaterials() {
        List<MaterialStockResponse> response = materialStockService.getLowStockMaterials();
        return ApiResponse.<List<MaterialStockResponse>>builder()
                .message("Low-stock materials retrieved successfully")
                .result(response)
                .build();
    }
}
