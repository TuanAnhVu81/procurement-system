package com.anhvt.epms.procurement.controller;

import com.anhvt.epms.procurement.dto.request.VendorRatingRequest;
import com.anhvt.epms.procurement.dto.request.VendorRequest;
import com.anhvt.epms.procurement.dto.response.ApiResponse;
import com.anhvt.epms.procurement.dto.response.VendorResponse;
import com.anhvt.epms.procurement.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for vendor management endpoints
 * Provides CRUD operations and vendor-specific features
 * 
 * Authorization Rules:
 * - ADMIN: Full access (Create, Read, Update, Delete)
 * - EMPLOYEE: Read-only access
 * - MANAGER: Read-only access
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
     * Required Role: ADMIN
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new vendor", description = "Create a new vendor with auto-generated vendor code")
    public ApiResponse<VendorResponse> createVendor(@Valid @RequestBody VendorRequest request) {
        VendorResponse response = vendorService.createVendor(request);
        
        return ApiResponse.<VendorResponse>builder()

                .message("Vendor created successfully")
                .result(response)
                .build();
    }
    
    /**
     * Get vendor by ID
     * GET /api/vendors/{id}
     * Required Role: ADMIN, EMPLOYEE, MANAGER
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'MANAGER')")
    @Operation(summary = "Get vendor by ID", description = "Retrieve a specific vendor by its ID")
    public ApiResponse<VendorResponse> getVendorById(@PathVariable UUID id) {
        VendorResponse response = vendorService.getVendorById(id);
        
        return ApiResponse.<VendorResponse>builder()

                .message("Vendor retrieved successfully")
                .result(response)
                .build();
    }
    
    /**
     * Update vendor
     * PUT /api/vendors/{id}
     * Required Role: ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update vendor", description = "Update vendor information (vendor code cannot be changed)")
    public ApiResponse<VendorResponse> updateVendor(
            @PathVariable UUID id,
            @Valid @RequestBody VendorRequest request) {
        VendorResponse response = vendorService.updateVendor(id, request);
        
        return ApiResponse.<VendorResponse>builder()

                .message("Vendor updated successfully")
                .result(response)
                .build();
    }
    
    /**
     * Update vendor rating
     * PUT /api/vendors/{id}/rating
     * Required Role: ADMIN (System/Admin only)
     */
    @PutMapping("/{id}/rating")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update vendor rating", description = "Update vendor rating based on delivery history (1.0-5.0)")
    public ApiResponse<VendorResponse> updateVendorRating(
            @PathVariable UUID id,
            @Valid @RequestBody VendorRatingRequest request) {
        VendorResponse response = vendorService.updateVendorRating(id, request);
        
        return ApiResponse.<VendorResponse>builder()

                .message("Vendor rating updated successfully")
                .result(response)
                .build();
    }
    
    /**
     * Delete vendor (soft delete)
     * DELETE /api/vendors/{id}
     * Required Role: ADMIN
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete vendor", description = "Soft delete vendor (sets status to INACTIVE, preserves data)")
    public ApiResponse<Void> deleteVendor(@PathVariable UUID id) {
        vendorService.deleteVendor(id);
        
        return ApiResponse.<Void>builder()

                .message("Vendor deleted successfully")
                .build();
    }
}
