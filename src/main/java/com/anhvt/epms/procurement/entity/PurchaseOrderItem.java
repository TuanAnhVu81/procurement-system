package com.anhvt.epms.procurement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * Purchase Order Item entity representing line items in a purchase order
 * Contains material, quantity, pricing, and calculated net amount
 */
@Entity
@Table(name = "purchase_order_items",
    indexes = {
        @Index(name = "idx_poi_po", columnList = "purchase_order_id"),
        @Index(name = "idx_poi_material", columnList = "material_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseOrderItem extends BaseEntity {
    
    @NotNull(message = "Purchase order is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    PurchaseOrder purchaseOrder;
    
    @NotNull(message = "Material is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    Material material;
    
    // Snapshot fields: Frozen at PO creation time for audit trail
    // These fields preserve material information even if master data changes
    @Column(name = "material_code", length = 30)
    String materialCode; // e.g., "MAT-2026-00001"
    
    @Column(name = "material_description", length = 500)
    String materialDescription; // e.g., "MacBook Pro M3 Max 16-inch"
    
    @Column(name = "unit", length = 20)
    String unit; // e.g., "PCS", "KG", "Box"
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(name = "quantity", nullable = false)
    Integer quantity;
    
    @NotNull(message = "Unit price is required")
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    BigDecimal unitPrice;
    
    @Column(name = "net_amount", precision = 15, scale = 2)
    BigDecimal netAmount; // quantity * unitPrice
    
    // Tax calculation at line item level (different items may have different tax rates)
    @Column(name = "tax_rate", precision = 5, scale = 4)
    @Builder.Default
    BigDecimal taxRate = new BigDecimal("0.10"); // Default 10% VAT
    
    @Column(name = "tax_amount", precision = 15, scale = 2)
    BigDecimal taxAmount; // netAmount * taxRate
    
    @Column(name = "line_total", precision = 15, scale = 2)
    BigDecimal lineTotal; // netAmount + taxAmount
    
    @Column(name = "line_number")
    Integer lineNumber;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    String notes;
    
    /**
     * Calculate amounts before persisting or updating
     * Formula:
     * - netAmount = quantity * unitPrice
     * - taxAmount = netAmount * taxRate
     * - lineTotal = netAmount + taxAmount
     */
    @PrePersist
    @PreUpdate
    public void calculateAmounts() {
        // Calculate net amount
        if (this.quantity != null && this.unitPrice != null) {
            this.netAmount = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        } else {
            this.netAmount = BigDecimal.ZERO;
        }
        
        // Calculate tax amount
        if (this.netAmount != null && this.taxRate != null) {
            this.taxAmount = this.netAmount.multiply(this.taxRate)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        } else {
            this.taxAmount = BigDecimal.ZERO;
        }
        
        // Calculate line total
        this.lineTotal = this.netAmount.add(this.taxAmount);
    }
}
