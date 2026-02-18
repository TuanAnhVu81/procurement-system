package com.anhvt.epms.procurement.entity;

import com.anhvt.epms.procurement.enums.POStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Purchase Order entity representing procurement orders
 * Header entity containing order information and financial totals
 */
@Entity
@Table(name = "purchase_orders",
    indexes = {
        @Index(name = "idx_po_number", columnList = "po_number", unique = true),
        @Index(name = "idx_po_status", columnList = "status"),
        @Index(name = "idx_po_vendor", columnList = "vendor_id"),
        @Index(name = "idx_po_date", columnList = "order_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseOrder extends BaseEntity {
    
    @NotBlank(message = "PO number is required")
    @Size(max = 30, message = "PO number must not exceed 30 characters")
    @Column(name = "po_number", nullable = false, unique = true, length = 30)
    String poNumber;
    
    @NotNull(message = "Vendor is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    Vendor vendor;
    
    @NotNull(message = "Order date is required")
    @Column(name = "order_date", nullable = false)
    LocalDate orderDate;
    
    @Column(name = "delivery_date")
    LocalDate deliveryDate;
    
    @Column(name = "delivery_address", length = 500)
    String deliveryAddress; // Shipping destination for goods receipt
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    POStatus status = POStatus.CREATED;
    
    @Column(name = "total_amount", precision = 15, scale = 2)
    @Builder.Default
    BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Column(name = "tax_amount", precision = 15, scale = 2)
    @Builder.Default
    BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(name = "grand_total", precision = 15, scale = 2)
    @Builder.Default
    BigDecimal grandTotal = BigDecimal.ZERO;
    
    @Column(name = "currency", length = 3)
    @Builder.Default
    String currency = "USD";
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    String rejectionReason;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    User approver;
    
    @Column(name = "approved_date")
    LocalDate approvedDate;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    String notes;
    
    // Relationships
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<PurchaseOrderItem> items = new ArrayList<>();
    
    // Helper methods
    public void addItem(PurchaseOrderItem item) {
        items.add(item);
        item.setPurchaseOrder(this);
        recalculateTotals();
    }
    
    public void removeItem(PurchaseOrderItem item) {
        items.remove(item);
        item.setPurchaseOrder(null);
        recalculateTotals();
    }
    
    /**
     * Recalculate total amounts based on line items
     * Called automatically when items are added or removed
     * 
     * New Formula (Item-level tax):
     * - totalAmount = SUM(items.netAmount)
     * - taxAmount = SUM(items.taxAmount)
     * - grandTotal = SUM(items.lineTotal) OR totalAmount + taxAmount
     */
    public void recalculateTotals() {
        // Calculate total net amount from all items
        this.totalAmount = items.stream()
            .map(PurchaseOrderItem::getNetAmount)
            .filter(amount -> amount != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate total tax amount from all items (each item may have different tax rate)
        this.taxAmount = items.stream()
            .map(PurchaseOrderItem::getTaxAmount)
            .filter(amount -> amount != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate grand total (can also sum lineTotal directly)
        this.grandTotal = this.totalAmount.add(this.taxAmount);
    }
    
    /**
     * Submit purchase order for approval
     */
    public void submit() {
        if (this.status == POStatus.CREATED) {
            this.status = POStatus.PENDING;
        }
    }
    
    /**
     * Approve purchase order
     */
    public void approve(User approver) {
        if (this.status == POStatus.PENDING) {
            this.status = POStatus.APPROVED;
            this.approver = approver;
            this.approvedDate = LocalDate.now();
        }
    }
    
    /**
     * Reject purchase order
     */
    public void reject(User approver, String reason) {
        if (this.status == POStatus.PENDING) {
            this.status = POStatus.REJECTED;
            this.approver = approver;
            this.rejectionReason = reason;
            this.approvedDate = LocalDate.now();
        }
    }
}
