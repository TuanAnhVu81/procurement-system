package com.anhvt.epms.procurement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Material entity representing items/products that can be procured
 * Manages material information including pricing and unit of measure
 */
@Entity
@Table(name = "materials",
    indexes = {
        @Index(name = "idx_material_code", columnList = "material_code", unique = true),
        @Index(name = "idx_category", columnList = "category")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Material extends BaseEntity {

    @Size(max = 50, message = "Material code must not exceed 50 characters")
    @Column(name = "material_code", nullable = false, unique = true, length = 50)
    String materialCode;
    
    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", nullable = false, length = 500)
    String description;
    
    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)
    BigDecimal basePrice;
    
    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    String currency = "USD";
    
    @Column(name = "unit", nullable = false, length = 20)
    @Builder.Default
    String unit = "PCS";
    
    @Column(name = "category", length = 100)
    String category;
    
    @Column(name = "manufacturer", length = 100)
    String manufacturer;
    
    @Column(name = "specifications", columnDefinition = "TEXT")
    String specifications;
    
    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;
    
    // Relationships
    @OneToOne(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true)
    MaterialStock stock;
    
    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL)
    @Builder.Default
    List<PurchaseOrderItem> purchaseOrderItems = new ArrayList<>();
    
    // Helper methods
    public void setStock(MaterialStock stock) {
        this.stock = stock;
        if (stock != null) {
            stock.setMaterial(this);
        }
    }
}
