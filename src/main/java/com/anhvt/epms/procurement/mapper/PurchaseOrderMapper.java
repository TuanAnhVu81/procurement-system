package com.anhvt.epms.procurement.mapper;

import com.anhvt.epms.procurement.dto.request.PurchaseOrderItemRequest;
import com.anhvt.epms.procurement.dto.request.PurchaseOrderRequest;
import com.anhvt.epms.procurement.dto.response.PurchaseOrderItemResponse;
import com.anhvt.epms.procurement.dto.response.PurchaseOrderResponse;
import com.anhvt.epms.procurement.dto.response.PurchaseOrderSummaryResponse;
import com.anhvt.epms.procurement.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between PurchaseOrder entities and DTOs
 * Handles complex nested mappings for vendor, items, and approver
 */
@Component
public class PurchaseOrderMapper implements BaseMapper {
    
    /**
     * Convert PurchaseOrder entity to full Response DTO
     */
    public PurchaseOrderResponse toResponse(PurchaseOrder entity) {
        if (entity == null) {
            return null;
        }
        
        return PurchaseOrderResponse.builder()
                .id(entity.getId())
                .poNumber(entity.getPoNumber())
                .vendor(mapVendorInfo(entity.getVendor()))
                .orderDate(entity.getOrderDate())
                .deliveryDate(entity.getDeliveryDate())
                .deliveryAddress(entity.getDeliveryAddress())
                .status(entity.getStatus())
                .statusDisplay(entity.getStatus() != null ? entity.getStatus().getDisplayName() : null)
                .totalAmount(entity.getTotalAmount())
                // taxRate is item-level field (PurchaseOrderItem), not on PO header entity
                .taxAmount(entity.getTaxAmount())
                .grandTotal(entity.getGrandTotal())
                .currency(entity.getCurrency())
                .items(mapItemsToResponse(entity.getItems()))
                .approver(mapApproverInfo(entity.getApprover()))
                .approvedDate(entity.getApprovedDate())
                .rejectionReason(entity.getRejectionReason())
                .notes(entity.getNotes())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .modifiedBy(entity.getUpdatedBy())
                .modifiedAt(entity.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert PurchaseOrder entity to Summary Response DTO (compact)
     */
    public PurchaseOrderSummaryResponse toSummaryResponse(PurchaseOrder entity) {
        if (entity == null) {
            return null;
        }
        
        return PurchaseOrderSummaryResponse.builder()
                .id(entity.getId())
                .poNumber(entity.getPoNumber())
                .vendorId(entity.getVendor() != null ? entity.getVendor().getId() : null)
                .vendorName(entity.getVendor() != null ? entity.getVendor().getName() : null)
                .orderDate(entity.getOrderDate())
                .deliveryDate(entity.getDeliveryDate())
                .status(entity.getStatus())
                .statusDisplay(entity.getStatus() != null ? entity.getStatus().getDisplayName() : null)
                .grandTotal(entity.getGrandTotal())
                .currency(entity.getCurrency())
                .itemCount(entity.getItems() != null ? entity.getItems().size() : 0)
                .approverName(entity.getApprover() != null ? entity.getApprover().getUsername() : null)
                .approvedDate(entity.getApprovedDate())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toLocalDate() : null)
                .build();
    }
    
    /**
     * Convert Request DTO to PurchaseOrder entity
     * Note: Vendor and Material entities must be fetched separately
     */
    public PurchaseOrder toEntity(PurchaseOrderRequest request) {
        if (request == null) {
            return null;
        }
        
        PurchaseOrder po = PurchaseOrder.builder()
                .orderDate(request.getOrderDate())
                .deliveryDate(request.getDeliveryDate())
                .deliveryAddress(request.getDeliveryAddress())
                .currency(request.getCurrency())
                // taxRate is item-level: each PurchaseOrderItem has its own taxRate field
                .notes(request.getNotes())
                .build();
        
        // Note: Vendor must be set separately in service layer
        // Note: Items will be mapped and added separately
        
        return po;
    }
    
    /**
     * Map list of PurchaseOrderItem entities to response DTOs
     */
    private List<PurchaseOrderItemResponse> mapItemsToResponse(List<PurchaseOrderItem> items) {
        if (items == null) {
            return null;
        }
        
        return items.stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Map single PurchaseOrderItem entity to response DTO
     */
    public PurchaseOrderItemResponse mapItemToResponse(PurchaseOrderItem item) {
        if (item == null) {
            return null;
        }
        
        return PurchaseOrderItemResponse.builder()
                .id(item.getId())
                .material(mapMaterialInfo(item.getMaterial()))
                .materialCode(item.getMaterialCode())
                .materialDescription(item.getMaterialDescription())
                .unit(item.getUnit())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .netAmount(item.getNetAmount())
                .taxRate(item.getTaxRate())
                .taxAmount(item.getTaxAmount())
                .lineTotal(item.getLineTotal())
                .lineNumber(item.getLineNumber())
                .notes(item.getNotes())
                .build();
    }
    
    /**
     * Map PurchaseOrderItemRequest to entity
     */
    public PurchaseOrderItem mapItemRequestToEntity(PurchaseOrderItemRequest request) {
        if (request == null) {
            return null;
        }
        
        return PurchaseOrderItem.builder()
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .taxRate(request.getTaxRate())
                .lineNumber(request.getLineNumber())
                .notes(request.getNotes())
                .build();
        // Note: Material and PurchaseOrder must be set in service layer
    }
    
    /**
     * Map Vendor entity to VendorInfo DTO
     */
    private PurchaseOrderResponse.VendorInfo mapVendorInfo(Vendor vendor) {
        if (vendor == null) {
            return null;
        }
        
        return PurchaseOrderResponse.VendorInfo.builder()
                .id(vendor.getId())
                .vendorCode(vendor.getVendorCode())
                .vendorName(vendor.getName())
                .contactPerson(vendor.getContactPerson())
                .email(vendor.getEmail())
                .phone(vendor.getPhone())
                .build();
    }
    
    /**
     * Map Material entity to MaterialInfo DTO
     */
    private PurchaseOrderItemResponse.MaterialInfo mapMaterialInfo(Material material) {
        if (material == null) {
            return null;
        }
        
        return PurchaseOrderItemResponse.MaterialInfo.builder()
                .id(material.getId())
                .materialCode(material.getMaterialCode())
                .materialName(material.getDescription())
                .description(material.getDescription())
                .unit(material.getUnit())
                .category(material.getCategory())
                .build();
    }
    
    /**
     * Map User entity to ApproverInfo DTO
     */
    private PurchaseOrderResponse.ApproverInfo mapApproverInfo(User approver) {
        if (approver == null) {
            return null;
        }
        
        return PurchaseOrderResponse.ApproverInfo.builder()
                .id(approver.getId())
                .username(approver.getUsername())
                .fullName(approver.getFullName())
                .email(approver.getEmail())
                .build();
    }
}
