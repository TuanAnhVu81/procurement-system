package com.anhvt.epms.procurement.service;

import com.anhvt.epms.procurement.dto.response.MaterialStockResponse;
import com.anhvt.epms.procurement.entity.PurchaseOrder;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing material inventory (stock)
 */
public interface MaterialStockService {

    /**
     * Get current stock information for a specific material
     * @param materialId the material UUID
     * @return stock details including quantity and low-stock flag
     */
    MaterialStockResponse getStockByMaterialId(UUID materialId);

    /**
     * Get all materials whose quantity is below the minimum stock level
     * Used for low-stock warnings on Manager/Admin dashboard
     * @return list of low-stock material records
     */
    List<MaterialStockResponse> getLowStockMaterials();

    /**
     * Process Goods Receipt for a given Purchase Order
     * Iterates each PurchaseOrderItem and adds the quantity to MaterialStock
     * Also emits low-stock warning if stock falls below minimum level
     * @param purchaseOrder the approved purchase order being received
     */
    void processGoodsReceipt(PurchaseOrder purchaseOrder);
}
