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
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(name = "quantity", nullable = false)
    Integer quantity;
    
    @NotNull(message = "Unit price is required")
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    BigDecimal unitPrice;
    
    @Column(name = "net_amount", precision = 15, scale = 2)
    BigDecimal netAmount;
    
    @Column(name = "line_number")
    Integer lineNumber;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    String notes;
    
    /**
     * Calculate net amount before persisting or updating
     * Net Amount = Quantity * Unit Price
     */
    @PrePersist
    @PreUpdate
    public void calculateNetAmount() {
        if (this.quantity != null && this.unitPrice != null) {
            this.netAmount = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        }
    }
}
