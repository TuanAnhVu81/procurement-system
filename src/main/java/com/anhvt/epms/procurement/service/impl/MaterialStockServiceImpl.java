package com.anhvt.epms.procurement.service.impl;

import com.anhvt.epms.procurement.dto.response.MaterialStockResponse;
import com.anhvt.epms.procurement.entity.Material;
import com.anhvt.epms.procurement.entity.MaterialStock;
import com.anhvt.epms.procurement.entity.PurchaseOrder;
import com.anhvt.epms.procurement.entity.PurchaseOrderItem;
import com.anhvt.epms.procurement.exception.AppException;
import com.anhvt.epms.procurement.exception.ErrorCode;
import com.anhvt.epms.procurement.repository.MaterialStockRepository;
import com.anhvt.epms.procurement.service.MaterialStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of MaterialStockService
 * Handles inventory queries and Goods Receipt processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialStockServiceImpl implements MaterialStockService {

    private final MaterialStockRepository materialStockRepository;

    @Override
    public MaterialStockResponse getStockByMaterialId(UUID materialId) {
        MaterialStock stock = materialStockRepository.findByMaterialId(materialId)
                .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_STOCK_NOT_FOUND));
        return toResponse(stock);
    }

    @Override
    public List<MaterialStockResponse> getLowStockMaterials() {
        // Query returns only materials where quantityOnHand < minimumStockLevel
        return materialStockRepository.findAllBelowMinimumStockLevel()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void processGoodsReceipt(PurchaseOrder purchaseOrder) {
        log.info("Processing Goods Receipt for PO: {}", purchaseOrder.getPoNumber());

        for (PurchaseOrderItem item : purchaseOrder.getItems()) {
            Material material = item.getMaterial();

            // Find existing stock record or create a new one if first time
            Optional<MaterialStock> existingStock = materialStockRepository.findByMaterialId(material.getId());

            MaterialStock stock;
            if (existingStock.isPresent()) {
                stock = existingStock.get();
            } else {
                // Auto-create a stock record for this material if it doesn't exist yet
                log.info("No stock record found for material '{}'. Creating new stock entry.", material.getMaterialCode());
                stock = MaterialStock.builder()
                        .material(material)
                        .quantityOnHand(0)
                        .reservedQuantity(0)
                        .availableQuantity(0)
                        .lastUpdated(LocalDateTime.now())
                        .build();
            }

            // Add received quantity from this PO line to the stock
            int received = item.getQuantity();
            stock.setQuantityOnHand(stock.getQuantityOnHand() + received);

            // Recalculate available quantity (JPA @PrePersist will also do this, but explicit is clearer)
            stock.setAvailableQuantity(stock.getQuantityOnHand() - stock.getReservedQuantity());
            stock.setLastUpdated(LocalDateTime.now());

            materialStockRepository.save(stock);

            log.info("Stock updated for '{}': +{} units, new total = {}",
                    material.getMaterialCode(), received, stock.getQuantityOnHand());

            // Emit low-stock warning if quantity falls below minimum threshold
            if (stock.getMinimumStockLevel() != null
                    && stock.getQuantityOnHand() < stock.getMinimumStockLevel()) {
                log.warn("[LOW STOCK WARNING] Material '{}' ({}): current={}, minimum={}",
                        material.getMaterialCode(),
                        material.getDescription(),
                        stock.getQuantityOnHand(),
                        stock.getMinimumStockLevel());
            }
        }

        log.info("Goods Receipt completed for PO: {}", purchaseOrder.getPoNumber());
    }

    /**
     * Convert MaterialStock entity to response DTO
     * @param stock entity to convert
     * @return MaterialStockResponse DTO
     */
    private MaterialStockResponse toResponse(MaterialStock stock) {
        boolean isLowStock = stock.getMinimumStockLevel() != null
                && stock.getQuantityOnHand() < stock.getMinimumStockLevel();

        return MaterialStockResponse.builder()
                .id(stock.getId())
                .materialId(stock.getMaterial().getId())
                .materialCode(stock.getMaterial().getMaterialCode())
                .materialDescription(stock.getMaterial().getDescription())
                .quantityOnHand(stock.getQuantityOnHand())
                .reservedQuantity(stock.getReservedQuantity())
                .availableQuantity(stock.getAvailableQuantity())
                .minimumStockLevel(stock.getMinimumStockLevel())
                .maximumStockLevel(stock.getMaximumStockLevel())
                .warehouseLocation(stock.getWarehouseLocation())
                .lowStock(isLowStock)
                .lastUpdated(stock.getLastUpdated())
                .build();
    }
}
