package com.anhvt.epms.procurement.controller;

import com.anhvt.epms.procurement.dto.request.MaterialRequest;
import com.anhvt.epms.procurement.dto.response.ApiResponse;
import com.anhvt.epms.procurement.dto.response.MaterialResponse;
import com.anhvt.epms.procurement.service.MaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for material management endpoints
 * Provides CRUD operations and material-specific features
 */
@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
@Tag(name = "Material Management", description = "APIs for managing materials (items/products)")
public class MaterialController {
    
    private final MaterialService materialService;
    
    /**
     * Create a new material
     * POST /api/materials
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new material", description = "Create a new material with auto-generated material code")
    public ApiResponse<MaterialResponse> createMaterial(@Valid @RequestBody MaterialRequest request) {
        MaterialResponse response = materialService.createMaterial(request);
        
        return ApiResponse.<MaterialResponse>builder()
                .code(1000)
                .message("Material created successfully")
                .result(response)
                .build();
    }
    
    /**
     * Get all materials with pagination
     * GET /api/materials
     */
    @GetMapping
    @Operation(summary = "Get all materials", description = "Retrieve all materials with pagination and sorting")
    public Page<MaterialResponse> getAllMaterials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return materialService.getAllMaterials(pageable);
    }
    
    /**
     * Get active materials only
     * GET /api/materials/active
     */
    @GetMapping("/active")
    @Operation(summary = "Get active materials", description = "Retrieve only active materials with pagination")
    public Page<MaterialResponse> getActiveMaterials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return materialService.getActiveMaterials(pageable);
    }
    
    /**
     * Get material by ID
     * GET /api/materials/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get material by ID", description = "Retrieve a specific material by its ID")
    public ApiResponse<MaterialResponse> getMaterialById(@PathVariable UUID id) {
        MaterialResponse response = materialService.getMaterialById(id);
        
        return ApiResponse.<MaterialResponse>builder()
                .code(1000)
                .message("Material retrieved successfully")
                .result(response)
                .build();
    }
    
    /**
     * Search material by code
     * GET /api/materials/search?code={materialCode}
     */
    @GetMapping("/search")
    @Operation(summary = "Search material by code", description = "Find material by unique material code")
    public ApiResponse<MaterialResponse> searchMaterialByCode(@RequestParam String code) {
        MaterialResponse response = materialService.getMaterialByCode(code);
        
        return ApiResponse.<MaterialResponse>builder()
                .code(1000)
                .message("Material found")
                .result(response)
                .build();
    }
    
    /**
     * Get materials by unit
     * GET /api/materials/unit/{unit}
     */
    @GetMapping("/unit/{unit}")
    @Operation(summary = "Get materials by unit", description = "Filter materials by unit of measure (KG, Box, PCS, etc.)")
    public ApiResponse<List<MaterialResponse>> getMaterialsByUnit(@PathVariable String unit) {
        List<MaterialResponse> response = materialService.getMaterialsByUnit(unit);
        
        return ApiResponse.<List<MaterialResponse>>builder()
                .code(1000)
                .message("Materials filtered by unit successfully")
                .result(response)
                .build();
    }
    
    /**
     * Get materials by category
     * GET /api/materials/category/{category}
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get materials by category", description = "Filter materials by category")
    public ApiResponse<List<MaterialResponse>> getMaterialsByCategory(@PathVariable String category) {
        List<MaterialResponse> response = materialService.getMaterialsByCategory(category);
        
        return ApiResponse.<List<MaterialResponse>>builder()
                .code(1000)
                .message("Materials filtered by category successfully")
                .result(response)
                .build();
    }
    
    /**
     * Update material
     * PUT /api/materials/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update material", description = "Update material information (material code cannot be changed)")
    public ApiResponse<MaterialResponse> updateMaterial(
            @PathVariable UUID id,
            @Valid @RequestBody MaterialRequest request) {
        MaterialResponse response = materialService.updateMaterial(id, request);
        
        return ApiResponse.<MaterialResponse>builder()
                .code(1000)
                .message("Material updated successfully")
                .result(response)
                .build();
    }
    
    /**
     * Delete material (soft delete)
     * DELETE /api/materials/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete material", description = "Soft delete material (sets isActive to false, preserves data)")
    public ApiResponse<Void> deleteMaterial(@PathVariable UUID id) {
        materialService.deleteMaterial(id);
        
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Material deleted successfully")
                .build();
    }
}
