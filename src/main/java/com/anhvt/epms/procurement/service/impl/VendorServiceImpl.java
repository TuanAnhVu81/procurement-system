package com.anhvt.epms.procurement.service.impl;

import com.anhvt.epms.procurement.dto.request.VendorRatingRequest;
import com.anhvt.epms.procurement.dto.request.VendorRequest;
import com.anhvt.epms.procurement.dto.response.VendorResponse;
import com.anhvt.epms.procurement.entity.Vendor;
import com.anhvt.epms.procurement.enums.Status;
import com.anhvt.epms.procurement.exception.AppException;
import com.anhvt.epms.procurement.exception.ErrorCode;
import com.anhvt.epms.procurement.mapper.VendorMapper;
import com.anhvt.epms.procurement.repository.VendorRepository;
import com.anhvt.epms.procurement.service.VendorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of VendorService
 * Handles all vendor management business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VendorServiceImpl implements VendorService {
    
    private final VendorRepository vendorRepository;
    private final VendorMapper vendorMapper;
    
    /**
     * Create a new vendor with auto-generated vendor code
     * Business rules:
     * - Vendor code format: VEN-{sequential_number} (e.g., VEN-001, VEN-002)
     * - Vendor code must be unique
     * - Default status is ACTIVE
     */
    @Override
    @Transactional
    public VendorResponse createVendor(VendorRequest request) {
        // 1. Check duplicate Tax ID (Mandatory unique)
        if (vendorRepository.existsByTaxId(request.getTaxId())) {
            throw new AppException(ErrorCode.VENDOR_TAX_ID_EXISTED);
        }

        // 2. Check duplicate Email (Optional warning)
        if (request.getEmail() != null && vendorRepository.existsByEmail(request.getEmail())) {
            log.warn("Creating vendor with existing email: {}", request.getEmail());
        }

        // Map DTO to Entity
        Vendor vendor = vendorMapper.toEntity(request);
        
        // 3. Auto-generate vendor code (VEN-YYYY-NNNN)
        // Ensure user cannot set this manually
        vendor.setVendorCode(generateVendorCode());
        
        // 4. Set system default fields
        if (vendor.getCategory() == null) {
            vendor.setCategory(com.anhvt.epms.procurement.enums.VendorCategory.DOMESTIC);
        }
        vendor.setStatus(Status.ACTIVE);
        vendor.setRating(0.0); // Default rating
        
        // Save vendor
        Vendor savedVendor = vendorRepository.save(vendor);
        
        log.info("Vendor '{}' created successfully with code: {}", 
                savedVendor.getName(), savedVendor.getVendorCode());
        
        return vendorMapper.toResponse(savedVendor);
    }
    
    /**
     * Generate vendor code with format: VEN-{YEAR}-{SEQUENCE}
     * Example: VEN-2025-0001
     */
    private String generateVendorCode() {
        int year = java.time.Year.now().getValue();
        String prefix = String.format("VEN-%d-", year);
        
        // Find latest vendor code for current year
        return vendorRepository.findTopByVendorCodeStartingWithOrderByVendorCodeDesc(prefix)
                .map(vendor -> {
                    String lastCode = vendor.getVendorCode();
                    // Extract sequence number (last 4 digits)
                    try {
                        String sequencePart = lastCode.substring(lastCode.lastIndexOf("-") + 1);
                        int sequence = Integer.parseInt(sequencePart);
                        return String.format("%s%04d", prefix, sequence + 1);
                    } catch (Exception e) {
                        log.warn("Failed to parse vendor code sequence: {}, falling back to 0001", lastCode);
                        return prefix + "0001";
                    }
                })
                .orElse(prefix + "0001");
    }
    
    /**
     * Get vendor by ID
     * @param id vendor ID
     * @return vendor response
     */
    @Override
    @Transactional(readOnly = true)
    public VendorResponse getVendorById(UUID id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VENDOR_NOT_FOUND));
        
        return vendorMapper.toResponse(vendor);
    }
    
    /**
     * Get vendor by unique vendor code
     * @param vendorCode unique vendor code
     * @return vendor response
     */
    @Override
    @Transactional(readOnly = true)
    public VendorResponse getVendorByCode(String vendorCode) {
        Vendor vendor = vendorRepository.findByVendorCode(vendorCode)
                .orElseThrow(() -> new AppException(ErrorCode.VENDOR_NOT_FOUND));
        
        return vendorMapper.toResponse(vendor);
    }
    
    /**
     * Get all vendors with pagination
     * @param pageable pagination information
     * @return page of vendors
     */
    @Override
    @Transactional(readOnly = true)
    public Page<VendorResponse> getAllVendors(Pageable pageable) {
        Page<Vendor> vendors = vendorRepository.findAll(pageable);
        return vendors.map(vendorMapper::toResponse);
    }
    
    /**
     * Get vendors filtered by status
     * @param status vendor status (ACTIVE/INACTIVE)
     * @param pageable pagination information
     * @return page of vendors with given status
     */
    @Override
    @Transactional(readOnly = true)
    public Page<VendorResponse> getVendorsByStatus(Status status, Pageable pageable) {
        Page<Vendor> vendors = vendorRepository.findByStatus(status, pageable);
        return vendors.map(vendorMapper::toResponse);
    }
    
    /**
     * Update vendor information
     * Note: Vendor code cannot be changed to maintain data integrity
     * @param id vendor ID
     * @param request vendor update request
     * @return updated vendor response
     */
    @Override
    @Transactional
    public VendorResponse updateVendor(UUID id, VendorRequest request) {
        // Find existing vendor
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VENDOR_NOT_FOUND));
        
        // Update entity from request using MapStruct
        // Note: vendorCode and rating are ignored in the mapper
        vendorMapper.updateEntityFromRequest(vendor, request);
        
        // Save updated vendor
        Vendor updatedVendor = vendorRepository.save(vendor);
        
        log.info("Vendor '{}' updated successfully", updatedVendor.getVendorCode());
        
        return vendorMapper.toResponse(updatedVendor);
    }
    
    /**
     * Update vendor rating based on delivery history
     * Business rules:
     * - Rating must be between 1.0 and 5.0
     * - Comment is optional but recommended
     * @param id vendor ID
     * @param request rating update request
     * @return updated vendor response
     */
    @Override
    @Transactional
    public VendorResponse updateVendorRating(UUID id, VendorRatingRequest request) {
        // Find existing vendor
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VENDOR_NOT_FOUND));
        
        // Validate rating range (additional validation beyond @DecimalMin/@DecimalMax)
        if (request.getRating() < 1.0 || request.getRating() > 5.0) {
            throw new AppException(ErrorCode.INVALID_VENDOR_RATING);
        }
        
        // Update rating
        vendor.setRating(request.getRating());
        
        // Update rating comment
        if (request.getComment() != null) {
            vendor.setRatingComment(request.getComment());
        }
        
        // Save updated vendor
        Vendor updatedVendor = vendorRepository.save(vendor);
        
        log.info("Vendor '{}' rating updated to: {}", updatedVendor.getVendorCode(), request.getRating());
        
        return vendorMapper.toResponse(updatedVendor);
    }
    
    /**
     * Soft delete vendor
     * Business rules:
     * - Does NOT physically delete from database (preserves history)
     * - Sets status to INACTIVE
     * - Vendor data remains for historical purchase orders
     * @param id vendor ID
     */
    @Override
    @Transactional
    public void deleteVendor(UUID id) {
        // Find existing vendor
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VENDOR_NOT_FOUND));
        
        // Soft delete: set status to INACTIVE
        vendor.setStatus(Status.INACTIVE);
        
        // Save updated vendor
        vendorRepository.save(vendor);
        
        log.info("Vendor '{}' soft deleted (set to INACTIVE)", vendor.getVendorCode());
    }

    /**
     * OData keyword search implementation
     * Delegates to JPA JPQL query that searches across name, vendorCode, email, contactPerson
     *
     * @param keyword  search keyword (case-insensitive)
     * @param pageable pagination
     * @return page of matching vendors
     */
    @Override
    @Transactional(readOnly = true)
    public Page<VendorResponse> searchByKeyword(String keyword, Pageable pageable) {
        // Delegate to repository JPQL query — no business logic needed
        Page<Vendor> vendors = vendorRepository.searchByKeyword(keyword, pageable);
        log.info("OData search vendors by keyword='{}', found={}", keyword, vendors.getTotalElements());
        return vendors.map(vendorMapper::toResponse);
    }
}
