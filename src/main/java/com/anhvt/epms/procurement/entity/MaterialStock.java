package com.anhvt.epms.procurement.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * MaterialStock entity for tracking inventory levels
 * One-to-One relationship with Material
 */
@Entity
@Table(name = "material_stocks",
    indexes = {
        @Index(name = "idx_material_stock", columnList = "material_id", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MaterialStock extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false, unique = true)
    Material material;
    
    @Column(name = "quantity_on_hand", nullable = false)
    @Builder.Default
    Integer quantityOnHand = 0;
    
    @Column(name = "reserved_quantity")
    @Builder.Default
    Integer reservedQuantity = 0;
    
    @Column(name = "available_quantity")
    @Builder.Default
    Integer availableQuantity = 0;
    
    @Column(name = "minimum_stock_level")
    Integer minimumStockLevel;
    
    @Column(name = "maximum_stock_level")
    Integer maximumStockLevel;
    
    @Column(name = "last_updated", nullable = false)
    LocalDateTime lastUpdated;
    
    @Column(name = "warehouse_location", length = 100)
    String warehouseLocation;
    
    @PrePersist
    @PreUpdate
    public void calculateAvailableQuantity() {
        this.availableQuantity = this.quantityOnHand - this.reservedQuantity;
        this.lastUpdated = LocalDateTime.now();
    }
}
