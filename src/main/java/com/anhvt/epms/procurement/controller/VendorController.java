package com.anhvt.epms.procurement.controller;

import com.anhvt.epms.procurement.dto.request.VendorRatingRequest;
import com.anhvt.epms.procurement.dto.request.VendorRequest;
import com.anhvt.epms.procurement.dto.response.ApiResponse;
import com.anhvt.epms.procurement.dto.response.VendorResponse;
import com.anhvt.epms.procurement.enums.Status;
import com.anhvt.epms.procurement.service.VendorService;
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

import java.util.UUID;

/**
 * REST Controller for vendor management endpoints
 * Provides CRUD operations and vendor-specific features
 */
@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
@Tag(name = "Vendor Management", description = "APIs for managing vendors (suppliers)")
public class VendorController {
    
    private final VendorService vendorService;
    
    /**
     * Create a new vendor
     * POST /api/vendors
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new vendor", description = "Create a new vendor with auto-generated vendor code")
    public ApiResponse<VendorResponse> createVendor(@Valid @RequestBody VendorRequest request) {
        VendorResponse response = vendorService.createVendor(request);
        
        return ApiResponse.<VendorResponse>builder()
                .code(1000)
                .message("Vendor created successfully")
                .result(response)
                .build();
    }
    
    /**
     * Get all vendors with pagination
     * GET /api/vendors
     */
    @GetMapping
    @Operation(summary = "Get all vendors", description = "Retrieve all vendors with pagination and sorting")
    public Page<VendorResponse> getAllVendors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return vendorService.getAllVendors(pageable);
    }
    
    /**
     * Get vendor by ID
     * GET /api/vendors/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get vendor by ID", description = "Retrieve a specific vendor by its ID")
    public ApiResponse<VendorResponse> getVendorById(@PathVariable UUID id) {
        VendorResponse response = vendorService.getVendorById(id);
        
        return ApiResponse.<VendorResponse>builder()
                .code(1000)
                .message("Vendor retrieved successfully")
                .result(response)
                .build();
    }
    
    /**
     * Search vendor by code
     * GET /api/vendors/search?code={vendorCode}
     */
    @GetMapping("/search")
    @Operation(summary = "Search vendor by code", description = "Find vendor by unique vendor code")
    public ApiResponse<VendorResponse> searchVendorByCode(@RequestParam String code) {
        VendorResponse response = vendorService.getVendorByCode(code);
        
        return ApiResponse.<VendorResponse>builder()
                .code(1000)
                .message("Vendor found")
                .result(response)
                .build();
    }
    
    /**
     * Get vendors by status
     * GET /api/vendors/status/{status}
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get vendors by status", description = "Filter vendors by status (ACTIVE/INACTIVE)")
    public Page<VendorResponse> getVendorsByStatus(
            @PathVariable Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return vendorService.getVendorsByStatus(status, pageable);
    }
    
    /**
     * Update vendor
     * PUT /api/vendors/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update vendor", description = "Update vendor information (vendor code cannot be changed)")
    public ApiResponse<VendorResponse> updateVendor(
            @PathVariable UUID id,
            @Valid @RequestBody VendorRequest request) {
        VendorResponse response = vendorService.updateVendor(id, request);
        
        return ApiResponse.<VendorResponse>builder()
                .code(1000)
                .message("Vendor updated successfully")
                .result(response)
                .build();
    }
    
    /**
     * Update vendor rating
     * PUT /api/vendors/{id}/rating
     */
    @PutMapping("/{id}/rating")
    @Operation(summary = "Update vendor rating", description = "Update vendor rating based on delivery history (1.0-5.0)")
    public ApiResponse<VendorResponse> updateVendorRating(
            @PathVariable UUID id,
            @Valid @RequestBody VendorRatingRequest request) {
        VendorResponse response = vendorService.updateVendorRating(id, request);
        
        return ApiResponse.<VendorResponse>builder()
                .code(1000)
                .message("Vendor rating updated successfully")
                .result(response)
                .build();
    }
    
    /**
     * Delete vendor (soft delete)
     * DELETE /api/vendors/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete vendor", description = "Soft delete vendor (sets status to INACTIVE, preserves data)")
    public ApiResponse<Void> deleteVendor(@PathVariable UUID id) {
        vendorService.deleteVendor(id);
        
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Vendor deleted successfully")
                .build();
    }
}
