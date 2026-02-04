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
        // Validate vendor code uniqueness if provided
        if (request.getVendorCode() != null && !request.getVendorCode().isEmpty()) {
            if (vendorRepository.existsByVendorCode(request.getVendorCode())) {
                throw new AppException(ErrorCode.VENDOR_CODE_EXISTED);
            }
        }
        
        // Map DTO to Entity using MapStruct
        Vendor vendor = vendorMapper.toEntity(request);
        
        // Auto-generate vendor code if not provided
        if (vendor.getVendorCode() == null || vendor.getVendorCode().isEmpty()) {
            vendor.setVendorCode(generateVendorCode());
        }
        
        // Set default values
        vendor.setStatus(Status.ACTIVE);
        vendor.setRating(null); // Rating will be set later based on delivery history
        
        // Save vendor to database
        Vendor savedVendor = vendorRepository.save(vendor);
        
        log.info("Vendor '{}' created successfully with code: {}", 
                savedVendor.getName(), savedVendor.getVendorCode());
        
        // Map Entity to Response DTO using MapStruct
        return vendorMapper.toResponse(savedVendor);
    }
    
    /**
     * Generate unique vendor code in format VEN-{sequential_number}
     * @return generated vendor code
     */
    private String generateVendorCode() {
        // Get the count of existing vendors and increment
        long count = vendorRepository.count();
        String vendorCode;
        
        // Loop until we find a unique code
        do {
            count++;
            vendorCode = String.format("VEN-%03d", count);
        } while (vendorRepository.existsByVendorCode(vendorCode));
        
        return vendorCode;
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
}
