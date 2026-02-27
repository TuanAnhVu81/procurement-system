package com.anhvt.epms.procurement.repository;

import com.anhvt.epms.procurement.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Material entity
 * Provides CRUD operations and custom queries for material management
 */
@Repository
public interface MaterialRepository extends JpaRepository<Material, UUID> {
    
    /**
     * Find material by unique material code
     * @param materialCode unique material code
     * @return Optional containing material if found
     */
    Optional<Material> findByMaterialCode(String materialCode);
    
    /**
     * Check if material code already exists
     * @param materialCode material code to check
     * @return true if exists, false otherwise
     */
    boolean existsByMaterialCode(String materialCode);
    
    /**
     * Find all materials by active status
     * @param isActive active status (true/false)
     * @return list of materials
     */
    List<Material> findByIsActive(Boolean isActive);
    
    /**
     * Find all active materials with pagination
     * @param pageable pagination information
     * @return page of active materials
     */
    Page<Material> findByIsActiveTrue(Pageable pageable);
    
    /**
     * Find materials by unit of measure
     * @param unit unit of measure (KG, Box, PCS, etc.)
     * @return list of materials with given unit
     */
    List<Material> findByUnit(String unit);
    
    /**
     * Find materials by category
     * @param category material category
     * @return list of materials in category
     */
    List<Material> findByCategory(String category);

    /**
     * Find latest material code by prefix to generate next sequence
     * @param prefix material code prefix (e.g., MAT-2024-)
     * @return Optional containing material with latest code
     */
    Optional<Material> findTopByMaterialCodeStartingWithOrderByMaterialCodeDesc(String prefix);

    /**
     * Check if material with same description exists (Simple Fuzzy Search)
     * @param description description to check
     * @return true if exists
     */
    boolean existsByDescriptionIgnoreCaseAndIsActiveTrue(String description);
    
    /**
     * Find all materials with pagination
     * @param pageable pagination information
     * @return page of materials
     */
    Page<Material> findAll(Pageable pageable);

    /**
     * OData keyword search across description and materialCode fields
     * Supports combined filter: contains(description,'kw') or contains(materialCode,'kw')
     *
     * @param keyword search keyword (case-insensitive)
     * @param pageable pagination
     * @return page of matching materials
     */
    @Query("SELECT m FROM Material m WHERE " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.materialCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Material> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * OData combined search: keyword + isActive filter
     * Supports: $filter=isActive eq true and contains(description,'kw')
     *
     * @param keyword search keyword
     * @param isActive active status filter
     * @param pageable pagination
     * @return page of matching materials
     */
    @Query("SELECT m FROM Material m WHERE m.isActive = :isActive AND (" +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.materialCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Material> searchByKeywordAndActive(
            @Param("keyword") String keyword,
            @Param("isActive") Boolean isActive,
            Pageable pageable);
}
