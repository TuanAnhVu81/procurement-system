package com.anhvt.epms.procurement.service;

import com.anhvt.epms.procurement.dto.response.PurchaseOrderResponse;
import com.anhvt.epms.procurement.entity.*;
import com.anhvt.epms.procurement.enums.POStatus;
import com.anhvt.epms.procurement.exception.ResourceNotFoundException;
import com.anhvt.epms.procurement.mapper.PurchaseOrderMapper;
import com.anhvt.epms.procurement.repository.*;
import com.anhvt.epms.procurement.service.impl.PurchaseOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PurchaseOrderServiceImpl
 * Focuses on the Goods Receipt (receive) flow and workflow status transitions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseOrderService Unit Tests")
class PurchaseOrderServiceTest {

    @Mock private PurchaseOrderRepository purchaseOrderRepository;
    @Mock private PurchaseOrderItemRepository purchaseOrderItemRepository;
    @Mock private VendorRepository vendorRepository;
    @Mock private MaterialRepository materialRepository;
    @Mock private UserRepository userRepository;
    @Mock private PurchaseOrderMapper purchaseOrderMapper;
    @Mock private MaterialStockService materialStockService;

    @InjectMocks
    private PurchaseOrderServiceImpl purchaseOrderService;

    // ─── Common test fixtures ───────────────────────────────────────────────

    private UUID poId;
    private PurchaseOrder approvedPO;
    private User currentUser;

    @BeforeEach
    void setUp() {
        poId = UUID.randomUUID();

        Material material = Material.builder()
                .materialCode("MAT-2026-00001")
                .description("MacBook Pro")
                .basePrice(new BigDecimal("3499.00"))
                .unit("PCS")
                .build();
        material.setId(UUID.randomUUID());

        PurchaseOrderItem item = PurchaseOrderItem.builder()
                .material(material)
                .quantity(5)
                .unitPrice(new BigDecimal("3499.00"))
                .taxRate(new BigDecimal("0.10"))
                .lineNumber(1)
                .build();

        // Build an APPROVED PO ready for Goods Receipt
        approvedPO = PurchaseOrder.builder()
                .poNumber("PO-2026-0001")
                .status(POStatus.APPROVED)
                .items(List.of(item))
                .build();
        approvedPO.setId(poId);

        currentUser = User.builder()
                .username("warehouse.staff")
                .password("encoded")
                .email("warehouse@epms.com")
                .fullName("Warehouse Staff")
                .build();
        currentUser.setId(UUID.randomUUID());
    }

    /**
     * Helper: Mock Spring SecurityContext so getCurrentUser() works in tests.
     * Uses lenient() to suppress UnnecessaryStubbingException in tests that
     * throw before the SecurityContext is actually accessed (e.g., PO not found).
     */
    private void mockSecurityContext(String username) {
        Authentication auth = mock(Authentication.class);
        SecurityContext ctx = mock(SecurityContext.class);
        lenient().when(auth.getName()).thenReturn(username);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
        lenient().when(userRepository.findByUsername(username)).thenReturn(Optional.of(currentUser));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GROUP 1: receivePurchaseOrder — Happy Path
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("receivePurchaseOrder() — Happy Path")
    class ReceivePurchaseOrderHappyPathTests {

        @Test
        @DisplayName("Should transition PO status from APPROVED to RECEIVED and call processGoodsReceipt")
        void shouldTransitionToReceived_andProcessGoodsReceipt() {
            // GIVEN
            mockSecurityContext("warehouse.staff");
            when(purchaseOrderRepository.findById(poId)).thenReturn(Optional.of(approvedPO));
            when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

            PurchaseOrderResponse expectedResponse = PurchaseOrderResponse.builder()
                    .poNumber("PO-2026-0001")
                    .status(POStatus.RECEIVED)
                    .build();
            when(purchaseOrderMapper.toResponse(any(PurchaseOrder.class))).thenReturn(expectedResponse);

            // WHEN
            PurchaseOrderResponse response = purchaseOrderService.receivePurchaseOrder(poId);

            // THEN 1: Status must be RECEIVED
            assertThat(response.getStatus()).isEqualTo(POStatus.RECEIVED);

            // THEN 2: processGoodsReceipt must be called exactly once with the PO
            verify(materialStockService, times(1)).processGoodsReceipt(approvedPO);

            // THEN 3: PO must be saved after status update
            verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
        }

        @Test
        @DisplayName("Should update the PO entity status to RECEIVED before saving")
        void shouldSetStatusToReceived_beforeSaving() {
            // GIVEN
            mockSecurityContext("warehouse.staff");
            when(purchaseOrderRepository.findById(poId)).thenReturn(Optional.of(approvedPO));
            when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));
            when(purchaseOrderMapper.toResponse(any())).thenReturn(new PurchaseOrderResponse());

            // WHEN
            purchaseOrderService.receivePurchaseOrder(poId);

            // THEN: The entity's status field must have been set to RECEIVED
            assertThat(approvedPO.getStatus()).isEqualTo(POStatus.RECEIVED);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GROUP 2: receivePurchaseOrder — Error Cases
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("receivePurchaseOrder() — Error Cases")
    class ReceivePurchaseOrderErrorTests {

        @Test
        @DisplayName("Should throw ResourceNotFoundException when PO ID does not exist")
        void shouldThrow_whenPoNotFound() {
            // GIVEN: PO ID does not exist in DB
            mockSecurityContext("warehouse.staff");
            when(purchaseOrderRepository.findById(poId)).thenReturn(Optional.empty());

            // WHEN + THEN
            assertThatThrownBy(() -> purchaseOrderService.receivePurchaseOrder(poId))
                    .isInstanceOf(ResourceNotFoundException.class);

            // processGoodsReceipt and save must NOT be called
            verify(materialStockService, never()).processGoodsReceipt(any());
            verify(purchaseOrderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalStateException when trying to receive a PENDING PO")
        void shouldThrow_whenPoStatusIsPending() {
            // GIVEN: PO is PENDING, not yet APPROVED
            mockSecurityContext("warehouse.staff");
            PurchaseOrder pendingPO = PurchaseOrder.builder()
                    .poNumber("PO-2026-0002")
                    .status(POStatus.PENDING)
                    .items(List.of())
                    .build();
            pendingPO.setId(poId);
            when(purchaseOrderRepository.findById(poId)).thenReturn(Optional.of(pendingPO));

            // WHEN + THEN: Can only receive APPROVED POs
            assertThatThrownBy(() -> purchaseOrderService.receivePurchaseOrder(poId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("PENDING");

            verify(materialStockService, never()).processGoodsReceipt(any());
        }

        @Test
        @DisplayName("Should throw IllegalStateException when trying to receive an already RECEIVED PO")
        void shouldThrow_whenPoAlreadyReceived() {
            // GIVEN: PO has already been received (RECEIVED status)
            mockSecurityContext("warehouse.staff");
            PurchaseOrder receivedPO = PurchaseOrder.builder()
                    .poNumber("PO-2026-0003")
                    .status(POStatus.RECEIVED)
                    .items(List.of())
                    .build();
            receivedPO.setId(poId);
            when(purchaseOrderRepository.findById(poId)).thenReturn(Optional.of(receivedPO));

            // WHEN + THEN
            assertThatThrownBy(() -> purchaseOrderService.receivePurchaseOrder(poId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("RECEIVED");

            verify(materialStockService, never()).processGoodsReceipt(any());
        }

        @Test
        @DisplayName("Should throw IllegalStateException when trying to receive a REJECTED PO")
        void shouldThrow_whenPoIsRejected() {
            // GIVEN: A rejected PO cannot be received
            mockSecurityContext("warehouse.staff");
            PurchaseOrder rejectedPO = PurchaseOrder.builder()
                    .poNumber("PO-2026-0004")
                    .status(POStatus.REJECTED)
                    .items(List.of())
                    .build();
            rejectedPO.setId(poId);
            when(purchaseOrderRepository.findById(poId)).thenReturn(Optional.of(rejectedPO));

            // WHEN + THEN
            assertThatThrownBy(() -> purchaseOrderService.receivePurchaseOrder(poId))
                    .isInstanceOf(IllegalStateException.class);

            verify(materialStockService, never()).processGoodsReceipt(any());
        }
    }
}
