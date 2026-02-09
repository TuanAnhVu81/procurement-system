package com.anhvt.epms.procurement.repository;

import com.anhvt.epms.procurement.entity.Vendor;
import com.anhvt.epms.procurement.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Vendor entity
 * Provides CRUD operations and custom queries for vendor management
 */
@Repository
public interface VendorRepository extends JpaRepository<Vendor, UUID> {
    
    /**
     * Find vendor by unique vendor code
     * @param vendorCode unique vendor code
     * @return Optional containing vendor if found
     */
    Optional<Vendor> findByVendorCode(String vendorCode);
    
    /**
     * Check if vendor code already exists
     * @param vendorCode vendor code to check
     * @return true if exists, false otherwise
     */
    boolean existsByVendorCode(String vendorCode);

    /**
     * Check if tax ID already exists
     * @param taxId tax ID to check
     * @return true if exists, false otherwise
     */
    boolean existsByTaxId(String taxId);

    /**
     * Check if email already exists
     * @param email email to check
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find latest vendor code by prefix to generate next sequence
     * @param prefix vendor code prefix (e.g., VEN-2024-)
     * @return Optional containing vendor with latest code
     */
    Optional<Vendor> findTopByVendorCodeStartingWithOrderByVendorCodeDesc(String prefix);
    
    /**
     * Find all vendors by status
     * @param status vendor status (ACTIVE/INACTIVE)
     * @return list of vendors with given status
     */
    List<Vendor> findByStatus(Status status);
    
    /**
     * Find vendors by status with pagination (excluding soft-deleted)
     * @param status vendor status
     * @param pageable pagination information
     * @return page of vendors
     */
    Page<Vendor> findByStatus(Status status, Pageable pageable);
    
    /**
     * Find all vendors excluding soft-deleted ones
     * @param pageable pagination information
     * @return page of active vendors
     */
    Page<Vendor> findAll(Pageable pageable);
}
