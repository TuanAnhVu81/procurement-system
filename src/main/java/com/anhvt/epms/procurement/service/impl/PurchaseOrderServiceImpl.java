package com.anhvt.epms.procurement.service.impl;

import com.anhvt.epms.procurement.dto.request.PurchaseOrderItemRequest;
import com.anhvt.epms.procurement.dto.request.PurchaseOrderRequest;
import com.anhvt.epms.procurement.dto.response.PurchaseOrderResponse;
import com.anhvt.epms.procurement.dto.response.PurchaseOrderSummaryResponse;
import com.anhvt.epms.procurement.entity.*;
import com.anhvt.epms.procurement.enums.POStatus;
import com.anhvt.epms.procurement.exception.ResourceNotFoundException;
import com.anhvt.epms.procurement.mapper.PurchaseOrderMapper;
import com.anhvt.epms.procurement.repository.*;
import com.anhvt.epms.procurement.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation for Purchase Order operations
 * Contains business logic for PO management, auto-calculations, and workflow
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final VendorRepository vendorRepository;
    private final MaterialRepository materialRepository;
    private final UserRepository userRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;
    
    /**
     * Create a new purchase order with auto-generated PO number
     */
    @Override
    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request) {
        log.info("Creating new purchase order for vendor ID: {}", request.getVendorId());
        
        // Fetch vendor entity
        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + request.getVendorId()));
        
        // Map request to entity
        PurchaseOrder purchaseOrder = purchaseOrderMapper.toEntity(request);
        
        // Set vendor
        purchaseOrder.setVendor(vendor);
        
        // Auto-generate PO number
        String poNumber = generatePoNumber();
        purchaseOrder.setPoNumber(poNumber);
        
        // Set default taxRate if not provided
        if (purchaseOrder.getTaxRate() == null) {
            purchaseOrder.setTaxRate(new java.math.BigDecimal("0.10"));
        }
        
        // Process and add items
        List<PurchaseOrderItem> items = new ArrayList<>();
        for (PurchaseOrderItemRequest itemRequest : request.getItems()) {
            // Fetch material
            Material material = materialRepository.findById(itemRequest.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException("Material not found with ID: " + itemRequest.getMaterialId()));
            
            // Map item request to entity
            PurchaseOrderItem item = purchaseOrderMapper.mapItemRequestToEntity(itemRequest);
            item.setMaterial(material);
            
            // Set unit price (auto-fill from material if not provided)
            if (item.getUnitPrice() == null) {
                item.setUnitPrice(material.getBasePrice());
            }
            
            item.setPurchaseOrder(purchaseOrder);
            
            items.add(item);
        }
        
        purchaseOrder.setItems(items);
        
        // Recalculate totals (taxAmount and grandTotal)
        purchaseOrder.recalculateTotals();
        
        // Save purchase order (cascade saves items)
        PurchaseOrder savedPO = purchaseOrderRepository.save(purchaseOrder);
        
        log.info("Created purchase order with PO number: {}", savedPO.getPoNumber());
        
        return purchaseOrderMapper.toResponse(savedPO);
    }
    
    /**
     * Update an existing purchase order
     * Can only update if status is CREATED
     */
    @Override
    @Transactional
    public PurchaseOrderResponse updatePurchaseOrder(UUID id, PurchaseOrderRequest request) {
        log.info("Updating purchase order ID: {}", id);
        
        // Fetch existing PO
        PurchaseOrder existingPO = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));
        
        // Check if PO can be updated (only CREATED status)
        if (existingPO.getStatus() != POStatus.CREATED) {
            throw new IllegalStateException("Cannot update Purchase Order with status: " + existingPO.getStatus());
        }
        
        // Fetch new vendor if changed
        if (!existingPO.getVendor().getId().equals(request.getVendorId())) {
            Vendor newVendor = vendorRepository.findById(request.getVendorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + request.getVendorId()));
            existingPO.setVendor(newVendor);
        }
        
        // Update header fields
        existingPO.setOrderDate(request.getOrderDate());
        existingPO.setDeliveryDate(request.getDeliveryDate());
        existingPO.setCurrency(request.getCurrency());
        existingPO.setTaxRate(request.getTaxRate());
        existingPO.setNotes(request.getNotes());
        
        // Clear existing items and add new ones
        existingPO.getItems().clear();
        
        for (PurchaseOrderItemRequest itemRequest : request.getItems()) {
            Material material = materialRepository.findById(itemRequest.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException("Material not found with ID: " + itemRequest.getMaterialId()));
            
            PurchaseOrderItem item = purchaseOrderMapper.mapItemRequestToEntity(itemRequest);
            item.setMaterial(material);
            
            // Set unit price (auto-fill from material if not provided)
            if (item.getUnitPrice() == null) {
                item.setUnitPrice(material.getBasePrice());
            }
            
            item.setPurchaseOrder(existingPO);
            
            existingPO.getItems().add(item);
        }
        
        // Recalculate totals
        existingPO.recalculateTotals();
        
        PurchaseOrder updatedPO = purchaseOrderRepository.save(existingPO);
        
        log.info("Updated purchase order: {}", updatedPO.getPoNumber());
        
        return purchaseOrderMapper.toResponse(updatedPO);
    }
    
    /**
     * Soft delete a purchase order
     */
    @Override
    @Transactional
    public void deletePurchaseOrder(UUID id) {
        log.info("Deleting purchase order ID: {}", id);
        
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));
        
        // Check if can be deleted (only CREATED status)
        if (purchaseOrder.getStatus() != POStatus.CREATED) {
            throw new IllegalStateException("Cannot delete Purchase Order with status: " + purchaseOrder.getStatus());
        }
        
        // Soft delete
        purchaseOrder.setStatus(POStatus.CANCELLED);
        purchaseOrderRepository.save(purchaseOrder);
        
        log.info("Soft deleted purchase order: {}", purchaseOrder.getPoNumber());
    }
    
    /**
     * Get all purchase orders (active only)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseOrderSummaryResponse> getAllPurchaseOrders(Pageable pageable) {
        log.info("Fetching all purchase orders, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        Page<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAllActive(pageable);
        
        return purchaseOrders.map(purchaseOrderMapper::toSummaryResponse);
    }
    
    /**
     * Get purchase order by ID with full details
     */
    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrderById(UUID id) {
        log.info("Fetching purchase order by ID: {}", id);
        
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));
        
        return purchaseOrderMapper.toResponse(purchaseOrder);
    }
    
    /**
     * Get purchase orders by status
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseOrderSummaryResponse> getPurchaseOrdersByStatus(POStatus status, Pageable pageable) {
        log.info("Fetching purchase orders with status: {}", status);
        
        Page<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findByStatus(status, pageable);
        
        return purchaseOrders.map(purchaseOrderMapper::toSummaryResponse);
    }
    
    /**
     * Search purchase orders by PO number keyword
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseOrderSummaryResponse> searchPurchaseOrders(String keyword, Pageable pageable) {
        log.info("Searching purchase orders with keyword: {}", keyword);
        
        Page<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findByPoNumberContainingIgnoreCase(keyword, pageable);
        
        return purchaseOrders.map(purchaseOrderMapper::toSummaryResponse);
    }
    
    /**
     * Submit purchase order for approval
     * Status: CREATED -> PENDING
     */
    @Override
    @Transactional
    public PurchaseOrderResponse submitForApproval(UUID id) {
        log.info("Submitting purchase order for approval, ID: {}", id);
        
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));
        
        // Use entity method for workflow transition
        purchaseOrder.submit();
        
        PurchaseOrder updatedPO = purchaseOrderRepository.save(purchaseOrder);
        
        log.info("Purchase order submitted for approval: {}", updatedPO.getPoNumber());
        
        return purchaseOrderMapper.toResponse(updatedPO);
    }
    
    /**
     * Approve a purchase order
     * Status: PENDING -> APPROVED
     */
    @Override
    @Transactional
    public PurchaseOrderResponse approvePurchaseOrder(UUID id) {
        log.info("Approving purchase order ID: {}", id);
        
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));
        
        // Get current user as approver
        User approver = getCurrentUser();
        
        // Use entity method for workflow transition
        purchaseOrder.approve(approver);
        
        PurchaseOrder updatedPO = purchaseOrderRepository.save(purchaseOrder);
        
        log.info("Purchase order approved: {} by {}", updatedPO.getPoNumber(), approver.getUsername());
        
        return purchaseOrderMapper.toResponse(updatedPO);
    }
    
    /**
     * Reject a purchase order with reason
     * Status: PENDING -> REJECTED
     */
    @Override
    @Transactional
    public PurchaseOrderResponse rejectPurchaseOrder(UUID id, String rejectionReason) {
        log.info("Rejecting purchase order ID: {} with reason: {}", id, rejectionReason);
        
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));
        
        // Get current user as approver
        User approver = getCurrentUser();
        
        // Use entity method for workflow transition
        purchaseOrder.reject(approver, rejectionReason);
        
        PurchaseOrder updatedPO = purchaseOrderRepository.save(purchaseOrder);
        
        log.info("Purchase order rejected: {} by {}", updatedPO.getPoNumber(), approver.getUsername());
        
        return purchaseOrderMapper.toResponse(updatedPO);
    }
    
    /**
     * Generate unique PO number with format: PO-YYYY-NNNN
     * Example: PO-2026-0001, PO-2026-0002, etc.
     */
    private String generatePoNumber() {
        int currentYear = LocalDate.now().getYear();
        
        // Get latest PO number
        String latestPoNumber = purchaseOrderRepository.findLatestPoNumber().orElse(null);
        
        int sequenceNumber = 1;
        
        if (latestPoNumber != null && latestPoNumber.startsWith("PO-" + currentYear)) {
            // Extract sequence number from latest PO
            try {
                String sequencePart = latestPoNumber.substring(latestPoNumber.lastIndexOf("-") + 1);
                sequenceNumber = Integer.parseInt(sequencePart) + 1;
            } catch (Exception e) {
                log.warn("Could not parse sequence from PO number: {}, using default sequence", latestPoNumber);
            }
        }
        
        // Format: PO-YYYY-NNNN
        return String.format("PO-%d-%04d", currentYear, sequenceNumber);
    }
    
    /**
     * Get current authenticated user
     * Temporary implementation - will be enhanced when authorization is added
     */
    private User getCurrentUser() {
        try {
            // Try to get from security context
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        } catch (Exception e) {
            // Fallback: return first user in database (for testing without auth)
            log.warn("Could not get current user from security context, using fallback");
            return userRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("No user found in system"));
        }
    }
}
