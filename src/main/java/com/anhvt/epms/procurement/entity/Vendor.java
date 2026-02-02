package com.anhvt.epms.procurement.entity;

import com.anhvt.epms.procurement.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

/**
 * Vendor entity representing suppliers in the procurement system
 * Manages vendor information including contact details and rating
 */
@Entity
@Table(name = "vendors",
    indexes = {
        @Index(name = "idx_vendor_code", columnList = "vendor_code", unique = true),
        @Index(name = "idx_vendor_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Vendor extends BaseEntity {
    
    @NotBlank(message = "Vendor code is required")
    @Size(max = 20, message = "Vendor code must not exceed 20 characters")
    @Column(name = "vendor_code", nullable = false, unique = true, length = 20)
    String vendorCode;
    
    @NotBlank(message = "Vendor name is required")
    @Size(max = 200, message = "Vendor name must not exceed 200 characters")
    @Column(name = "name", nullable = false, length = 200)
    String name;
    
    @Email(message = "Invalid email format")
    @Column(name = "email", length = 100)
    String email;
    
    @Column(name = "phone", length = 20)
    String phone;
    
    @Column(name = "address", length = 500)
    String address;
    
    @Column(name = "rating")
    Double rating;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    Status status = Status.ACTIVE;
    
    @Column(name = "tax_id", length = 50)
    String taxId;
    
    @Column(name = "contact_person", length = 100)
    String contactPerson;
    
    @Column(name = "payment_terms", length = 100)
    String paymentTerms;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    String notes;
    
    // Relationships
    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL)
    @Builder.Default
    List<PurchaseOrder> purchaseOrders = new ArrayList<>();
    
    // Helper methods
    public void addPurchaseOrder(PurchaseOrder purchaseOrder) {
        purchaseOrders.add(purchaseOrder);
        purchaseOrder.setVendor(this);
    }
}
