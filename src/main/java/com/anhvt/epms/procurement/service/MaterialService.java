package com.anhvt.epms.procurement.service;

import com.anhvt.epms.procurement.dto.request.MaterialRequest;
import com.anhvt.epms.procurement.dto.response.MaterialResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for material management operations
 * Provides business logic for CRUD operations and material-specific features
 */
public interface MaterialService {
    
    /**
     * Create a new material with auto-generated material code
     * @param request material creation request
     * @return created material response
     */
    MaterialResponse createMaterial(MaterialRequest request);
    
    /**
     * Get material by ID
     * @param id material ID
     * @return material response
     */
    MaterialResponse getMaterialById(UUID id);
    
    /**
     * Get material by unique material code
     * @param materialCode unique material code
     * @return material response
     */
    MaterialResponse getMaterialByCode(String materialCode);
    
    /**
     * Get all materials with pagination
     * @param pageable pagination information
     * @return page of materials
     */
    Page<MaterialResponse> getAllMaterials(Pageable pageable);
    
    /**
     * Get only active materials with pagination
     * @param pageable pagination information
     * @return page of active materials
     */
    Page<MaterialResponse> getActiveMaterials(Pageable pageable);
    
    /**
     * Get materials filtered by unit of measure
     * @param unit unit of measure (KG, Box, PCS, etc.)
     * @return list of materials with given unit
     */
    List<MaterialResponse> getMaterialsByUnit(String unit);
    
    /**
     * Get materials filtered by category
     * @param category material category
     * @return list of materials in category
     */
    List<MaterialResponse> getMaterialsByCategory(String category);
    
    /**
     * Update material information
     * Note: Material code cannot be changed
     * @param id material ID
     * @param request material update request
     * @return updated material response
     */
    MaterialResponse updateMaterial(UUID id, MaterialRequest request);
    
    /**
     * Soft delete material (set isActive to false)
     * Preserves data for historical purchase orders
     * @param id material ID
     */
    void deleteMaterial(UUID id);
}
