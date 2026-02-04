package com.anhvt.epms.procurement.service.impl;

import com.anhvt.epms.procurement.dto.request.MaterialRequest;
import com.anhvt.epms.procurement.dto.response.MaterialResponse;
import com.anhvt.epms.procurement.entity.Material;
import com.anhvt.epms.procurement.exception.AppException;
import com.anhvt.epms.procurement.exception.ErrorCode;
import com.anhvt.epms.procurement.mapper.MaterialMapper;
import com.anhvt.epms.procurement.repository.MaterialRepository;
import com.anhvt.epms.procurement.service.MaterialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of MaterialService
 * Handles all material management business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialServiceImpl implements MaterialService {
    
    private final MaterialRepository materialRepository;
    private final MaterialMapper materialMapper;
    
    /**
     * Create a new material with auto-generated material code
     * Business rules:
     * - Material code format: MAT-{sequential_number} (e.g., MAT-001, MAT-002)
     * - Material code must be unique
     * - Base price must be greater than 0
     * - Currency must be USD, VND, or EUR
     * - Default status is ACTIVE
     */
    @Override
    @Transactional
    public MaterialResponse createMaterial(MaterialRequest request) {
        // Validate material code uniqueness
        if (materialRepository.existsByMaterialCode(request.getMaterialCode())) {
            throw new AppException(ErrorCode.MATERIAL_CODE_EXISTED);
        }
        
        // Validate base price
        if (request.getBasePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INVALID_MATERIAL_PRICE);
        }
        
        // Validate currency
        if (!isValidCurrency(request.getCurrency())) {
            throw new AppException(ErrorCode.INVALID_CURRENCY);
        }
        
        // Map DTO to Entity using MapStruct
        Material material = materialMapper.toEntity(request);
        
        // Auto-generate material code if not provided
        if (material.getMaterialCode() == null || material.getMaterialCode().isEmpty()) {
            material.setMaterialCode(generateMaterialCode());
        }
        
        // Set default values
        material.setIsActive(true);
        
        // Save material to database
        Material savedMaterial = materialRepository.save(material);
        
        log.info("Material '{}' created successfully with code: {}", 
                savedMaterial.getDescription(), savedMaterial.getMaterialCode());
        
        // Map Entity to Response DTO using MapStruct
        return materialMapper.toResponse(savedMaterial);
    }
    
    /**
     * Generate unique material code in format MAT-{sequential_number}
     * @return generated material code
     */
    private String generateMaterialCode() {
        // Get the count of existing materials and increment
        long count = materialRepository.count();
        String materialCode;
        
        // Loop until we find a unique code
        do {
            count++;
            materialCode = String.format("MAT-%03d", count);
        } while (materialRepository.existsByMaterialCode(materialCode));
        
        return materialCode;
    }
    
    /**
     * Validate currency code
     * @param currency currency code to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidCurrency(String currency) {
        return "USD".equals(currency) || "VND".equals(currency) || "EUR".equals(currency);
    }
    
    /**
     * Get material by ID
     * @param id material ID
     * @return material response
     */
    @Override
    @Transactional(readOnly = true)
    public MaterialResponse getMaterialById(UUID id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_NOT_FOUND));
        
        return materialMapper.toResponse(material);
    }
    
    /**
     * Get material by unique material code
     * @param materialCode unique material code
     * @return material response
     */
    @Override
    @Transactional(readOnly = true)
    public MaterialResponse getMaterialByCode(String materialCode) {
        Material material = materialRepository.findByMaterialCode(materialCode)
                .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_NOT_FOUND));
        
        return materialMapper.toResponse(material);
    }
    
    /**
     * Get all materials with pagination
     * @param pageable pagination information
     * @return page of materials
     */
    @Override
    @Transactional(readOnly = true)
    public Page<MaterialResponse> getAllMaterials(Pageable pageable) {
        Page<Material> materials = materialRepository.findAll(pageable);
        return materials.map(materialMapper::toResponse);
    }
    
    /**
     * Get only active materials with pagination
     * @param pageable pagination information
     * @return page of active materials
     */
    @Override
    @Transactional(readOnly = true)
    public Page<MaterialResponse> getActiveMaterials(Pageable pageable) {
        Page<Material> materials = materialRepository.findByIsActiveTrue(pageable);
        return materials.map(materialMapper::toResponse);
    }
    
    /**
     * Get materials filtered by unit of measure
     * @param unit unit of measure (KG, Box, PCS, etc.)
     * @return list of materials with given unit
     */
    @Override
    @Transactional(readOnly = true)
    public List<MaterialResponse> getMaterialsByUnit(String unit) {
        List<Material> materials = materialRepository.findByUnit(unit);
        return materialMapper.toResponseList(materials);
    }
    
    /**
     * Get materials filtered by category
     * @param category material category
     * @return list of materials in category
     */
    @Override
    @Transactional(readOnly = true)
    public List<MaterialResponse> getMaterialsByCategory(String category) {
        List<Material> materials = materialRepository.findByCategory(category);
        return materialMapper.toResponseList(materials);
    }
    
    /**
     * Update material information
     * Note: Material code cannot be changed to maintain data integrity
     * @param id material ID
     * @param request material update request
     * @return updated material response
     */
    @Override
    @Transactional
    public MaterialResponse updateMaterial(UUID id, MaterialRequest request) {
        // Find existing material
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_NOT_FOUND));
        
        // Validate base price
        if (request.getBasePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INVALID_MATERIAL_PRICE);
        }
        
        // Validate currency
        if (!isValidCurrency(request.getCurrency())) {
            throw new AppException(ErrorCode.INVALID_CURRENCY);
        }
        
        // Update entity from request using MapStruct
        // Note: materialCode and isActive are ignored in the mapper
        materialMapper.updateEntityFromRequest(material, request);
        
        // Save updated material
        Material updatedMaterial = materialRepository.save(material);
        
        log.info("Material '{}' updated successfully", updatedMaterial.getMaterialCode());
        
        return materialMapper.toResponse(updatedMaterial);
    }
    
    /**
     * Soft delete material
     * Business rules:
     * - Does NOT physically delete from database (preserves history)
     * - Sets isActive to false
     * - Material data remains for historical purchase orders
     * @param id material ID
     */
    @Override
    @Transactional
    public void deleteMaterial(UUID id) {
        // Find existing material
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_NOT_FOUND));
        
        // Soft delete: set isActive to false
        material.setIsActive(false);
        
        // Save updated material
        materialRepository.save(material);
        
        log.info("Material '{}' soft deleted (set to INACTIVE)", material.getMaterialCode());
    }
}
