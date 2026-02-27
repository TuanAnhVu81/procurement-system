package com.anhvt.epms.procurement.service;

import com.anhvt.epms.procurement.dto.request.VendorRatingRequest;
import com.anhvt.epms.procurement.dto.request.VendorRequest;
import com.anhvt.epms.procurement.dto.response.VendorResponse;
import com.anhvt.epms.procurement.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for vendor management operations
 * Provides business logic for CRUD operations and vendor-specific features
 */
public interface VendorService {
    
    /**
     * Create a new vendor with auto-generated vendor code
     * @param request vendor creation request
     * @return created vendor response
     */
    VendorResponse createVendor(VendorRequest request);
    
    /**
     * Get vendor by ID
     * @param id vendor ID
     * @return vendor response
     */
    VendorResponse getVendorById(UUID id);
    
    /**
     * Get vendor by unique vendor code
     * @param vendorCode unique vendor code
     * @return vendor response
     */
    VendorResponse getVendorByCode(String vendorCode);
    
    /**
     * Get all vendors with pagination
     * @param pageable pagination information
     * @return page of vendors
     */
    Page<VendorResponse> getAllVendors(Pageable pageable);
    
    /**
     * Get vendors filtered by status
     * @param status vendor status (ACTIVE/INACTIVE)
     * @param pageable pagination information
     * @return page of vendors with given status
     */
    Page<VendorResponse> getVendorsByStatus(Status status, Pageable pageable);
    
    /**
     * Update vendor information
     * Note: Vendor code cannot be changed
     * @param id vendor ID
     * @param request vendor update request
     * @return updated vendor response
     */
    VendorResponse updateVendor(UUID id, VendorRequest request);
    
    /**
     * Update vendor rating based on delivery history
     * @param id vendor ID
     * @param request rating update request
     * @return updated vendor response
     */
    VendorResponse updateVendorRating(UUID id, VendorRatingRequest request);
    
    /**
     * Soft delete vendor (set status to INACTIVE)
     * Preserves data for historical purchase orders
     * @param id vendor ID
     */
    void deleteVendor(UUID id);

    /**
     * OData keyword search: search vendors by name, vendorCode, email, or contactPerson
     * Used by /odata/Vendors endpoint to support $filter=contains(...) queries
     *
     * @param keyword search keyword
     * @param pageable pagination information
     * @return page of matching vendors
     */
    Page<VendorResponse> searchByKeyword(String keyword, Pageable pageable);
}
