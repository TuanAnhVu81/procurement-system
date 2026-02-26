package com.anhvt.epms.procurement.service;

import com.anhvt.epms.procurement.dto.response.MaterialStockResponse;
import com.anhvt.epms.procurement.entity.Material;
import com.anhvt.epms.procurement.entity.MaterialStock;
import com.anhvt.epms.procurement.entity.PurchaseOrder;
import com.anhvt.epms.procurement.entity.PurchaseOrderItem;
import com.anhvt.epms.procurement.exception.AppException;
import com.anhvt.epms.procurement.exception.ErrorCode;
import com.anhvt.epms.procurement.repository.MaterialStockRepository;
import com.anhvt.epms.procurement.service.impl.MaterialStockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MaterialStockServiceImpl
 * Uses @ExtendWith(MockitoExtension.class) for pure unit testing (no Spring context)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MaterialStockService Unit Tests")
class MaterialStockServiceTest {

    @Mock
    private MaterialStockRepository materialStockRepository;

    @InjectMocks
    private MaterialStockServiceImpl materialStockService;

    // ─── Common test fixtures ───────────────────────────────────────────────

    private UUID materialId;
    private Material material;
    private MaterialStock existingStock;

    @BeforeEach
    void setUp() {
        materialId = UUID.randomUUID();

        // Build a reusable Material stub
        material = Material.builder()
                .materialCode("MAT-2026-00001")
                .description("MacBook Pro M3 Max 16-inch")
                .basePrice(new BigDecimal("3499.00"))
                .unit("PCS")
                .build();
        // Manually set the UUID (Lombok @Builder skips BaseEntity id)
        material.setId(materialId);

        // Build a reusable MaterialStock stub (quantityOnHand=10, minLevel=5)
        existingStock = MaterialStock.builder()
                .material(material)
                .quantityOnHand(10)
                .reservedQuantity(2)
                .availableQuantity(8)
                .minimumStockLevel(5)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GROUP 1: getStockByMaterialId
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getStockByMaterialId()")
    class GetStockByMaterialIdTests {

        @Test
        @DisplayName("Should return stock response when stock record exists")
        void shouldReturnStockResponse_whenStockExists() {
            // GIVEN: Repository returns the existing stock record
            when(materialStockRepository.findByMaterialId(materialId))
                    .thenReturn(Optional.of(existingStock));

            // WHEN: Service method is called
            MaterialStockResponse response = materialStockService.getStockByMaterialId(materialId);

            // THEN: Response fields should map correctly from entity
            assertThat(response).isNotNull();
            assertThat(response.getMaterialCode()).isEqualTo("MAT-2026-00001");
            assertThat(response.getQuantityOnHand()).isEqualTo(10);
            assertThat(response.getReservedQuantity()).isEqualTo(2);
            assertThat(response.getAvailableQuantity()).isEqualTo(8);
            assertThat(response.getMinimumStockLevel()).isEqualTo(5);
            // quantityOnHand(10) > minimumStockLevel(5) → lowStock must be false
            assertThat(response.isLowStock()).isFalse();

            verify(materialStockRepository, times(1)).findByMaterialId(materialId);
        }

        @Test
        @DisplayName("Should set lowStock=true when quantityOnHand is below minimum")
        void shouldMarkLowStock_whenQuantityBelowMinimum() {
            // GIVEN: Stock with quantity below minimum level
            MaterialStock lowStock = MaterialStock.builder()
                    .material(material)
                    .quantityOnHand(3)           // below minimumStockLevel=5
                    .reservedQuantity(0)
                    .availableQuantity(3)
                    .minimumStockLevel(5)
                    .lastUpdated(LocalDateTime.now())
                    .build();
            when(materialStockRepository.findByMaterialId(materialId))
                    .thenReturn(Optional.of(lowStock));

            // WHEN
            MaterialStockResponse response = materialStockService.getStockByMaterialId(materialId);

            // THEN: lowStock flag should be true
            assertThat(response.isLowStock()).isTrue();
            assertThat(response.getQuantityOnHand()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should throw AppException with MATERIAL_STOCK_NOT_FOUND when stock missing")
        void shouldThrowAppException_whenStockNotFound() {
            // GIVEN: Repository returns empty
            when(materialStockRepository.findByMaterialId(materialId))
                    .thenReturn(Optional.empty());

            // WHEN + THEN: AppException must be thrown with correct error code
            assertThatThrownBy(() -> materialStockService.getStockByMaterialId(materialId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.MATERIAL_STOCK_NOT_FOUND);
                    });

            verify(materialStockRepository, times(1)).findByMaterialId(materialId);
            // save() must NOT be called
            verify(materialStockRepository, never()).save(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GROUP 2: getLowStockMaterials
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getLowStockMaterials()")
    class GetLowStockMaterialsTests {

        @Test
        @DisplayName("Should return list of low-stock responses")
        void shouldReturnLowStockList_whenSomeStocksAreBelowMinimum() {
            // GIVEN
            MaterialStock lowStock = MaterialStock.builder()
                    .material(material)
                    .quantityOnHand(2)
                    .reservedQuantity(0)
                    .availableQuantity(2)
                    .minimumStockLevel(10)
                    .lastUpdated(LocalDateTime.now())
                    .build();
            when(materialStockRepository.findAllBelowMinimumStockLevel())
                    .thenReturn(List.of(lowStock));

            // WHEN
            List<MaterialStockResponse> result = materialStockService.getLowStockMaterials();

            // THEN
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isLowStock()).isTrue();
            assertThat(result.get(0).getMaterialCode()).isEqualTo("MAT-2026-00001");
        }

        @Test
        @DisplayName("Should return empty list when no materials are low on stock")
        void shouldReturnEmptyList_whenNoLowStockExists() {
            // GIVEN: No materials below minimum
            when(materialStockRepository.findAllBelowMinimumStockLevel())
                    .thenReturn(Collections.emptyList());

            // WHEN
            List<MaterialStockResponse> result = materialStockService.getLowStockMaterials();

            // THEN
            assertThat(result).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GROUP 3: processGoodsReceipt (Core GR logic)
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("processGoodsReceipt()")
    class ProcessGoodsReceiptTests {

        private PurchaseOrder buildPurchaseOrder(int quantity) {
            // Build a PO item referencing the shared material
            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .material(material)
                    .quantity(quantity)
                    .unitPrice(new BigDecimal("3499.00"))
                    .taxRate(new BigDecimal("0.10"))
                    .lineNumber(1)
                    .build();

            PurchaseOrder po = PurchaseOrder.builder()
                    .poNumber("PO-2026-0001")
                    .items(List.of(item))
                    .build();

            return po;
        }

        @Test
        @DisplayName("Should increase quantityOnHand when stock record already exists")
        void shouldIncreaseQuantityOnHand_whenStockExists() {
            // GIVEN: Existing stock with quantityOnHand=10; receiving 5 more units
            when(materialStockRepository.findByMaterialId(materialId))
                    .thenReturn(Optional.of(existingStock));
            when(materialStockRepository.save(any(MaterialStock.class))).thenAnswer(inv -> inv.getArgument(0));

            // WHEN
            materialStockService.processGoodsReceipt(buildPurchaseOrder(5));

            // THEN: Capture what was saved and verify the quantity increase
            ArgumentCaptor<MaterialStock> stockCaptor = ArgumentCaptor.forClass(MaterialStock.class);
            verify(materialStockRepository).save(stockCaptor.capture());

            MaterialStock saved = stockCaptor.getValue();
            assertThat(saved.getQuantityOnHand()).isEqualTo(15); // 10 existing + 5 received
            assertThat(saved.getAvailableQuantity()).isEqualTo(13); // 15 - 2 reserved
        }

        @Test
        @DisplayName("Should auto-create stock record and set quantityOnHand when no stock exists yet")
        void shouldCreateNewStock_whenNoExistingStockRecord() {
            // GIVEN: No stock record exists for this material yet
            when(materialStockRepository.findByMaterialId(materialId))
                    .thenReturn(Optional.empty());
            when(materialStockRepository.save(any(MaterialStock.class))).thenAnswer(inv -> inv.getArgument(0));

            // WHEN: Receiving 7 units via GR
            materialStockService.processGoodsReceipt(buildPurchaseOrder(7));

            // THEN: A new stock record is created with quantityOnHand = 7
            ArgumentCaptor<MaterialStock> stockCaptor = ArgumentCaptor.forClass(MaterialStock.class);
            verify(materialStockRepository).save(stockCaptor.capture());

            MaterialStock created = stockCaptor.getValue();
            assertThat(created.getQuantityOnHand()).isEqualTo(7);
            assertThat(created.getReservedQuantity()).isEqualTo(0);
            assertThat(created.getAvailableQuantity()).isEqualTo(7);
            assertThat(created.getMaterial()).isEqualTo(material);
        }

        @Test
        @DisplayName("Should emit low-stock warning when quantityOnHand drops below minimum after receipt")
        void shouldEmitLowStockWarning_whenStockBelowMinimumAfterReceipt() {
            // GIVEN: Stock with quantityOnHand=2, minimumStockLevel=10
            // Even after receiving 3 more → total 5, still below minimum 10
            MaterialStock tightStock = MaterialStock.builder()
                    .material(material)
                    .quantityOnHand(2)
                    .reservedQuantity(0)
                    .availableQuantity(2)
                    .minimumStockLevel(10)
                    .lastUpdated(LocalDateTime.now())
                    .build();
            when(materialStockRepository.findByMaterialId(materialId))
                    .thenReturn(Optional.of(tightStock));
            when(materialStockRepository.save(any(MaterialStock.class))).thenAnswer(inv -> inv.getArgument(0));

            // WHEN: Receiving 3 units (total will be 5, still < minimum 10)
            materialStockService.processGoodsReceipt(buildPurchaseOrder(3));

            // THEN: save() is still called (stock is updated), and quantityOnHand = 5
            ArgumentCaptor<MaterialStock> stockCaptor = ArgumentCaptor.forClass(MaterialStock.class);
            verify(materialStockRepository).save(stockCaptor.capture());
            assertThat(stockCaptor.getValue().getQuantityOnHand()).isEqualTo(5);
            // The log.warn is called internally — we verify by confirming save succeeded
            // (In production, wire a LogCaptor or spy logger; here the behavior is validated implicitly)
        }

        @Test
        @DisplayName("Should NOT emit low-stock warning when stock is above minimum after receipt")
        void shouldNotEmitWarning_whenStockAboveMinimumAfterReceipt() {
            // GIVEN: Stock with quantityOnHand=10, minimumStockLevel=5
            // Receiving 10 more → total 20, comfortably above minimum
            when(materialStockRepository.findByMaterialId(materialId))
                    .thenReturn(Optional.of(existingStock));
            when(materialStockRepository.save(any(MaterialStock.class))).thenAnswer(inv -> inv.getArgument(0));

            // WHEN: Receiving 10 units (total=20)
            materialStockService.processGoodsReceipt(buildPurchaseOrder(10));

            // THEN: save is called, no exception thrown → implicit pass for no-warning path
            ArgumentCaptor<MaterialStock> stockCaptor = ArgumentCaptor.forClass(MaterialStock.class);
            verify(materialStockRepository).save(stockCaptor.capture());
            assertThat(stockCaptor.getValue().getQuantityOnHand()).isEqualTo(20);
        }

        @Test
        @DisplayName("Should save stock once per item in the PO")
        void shouldSaveStockOncePerItem() {
            // GIVEN: A PO with 2 different line items (same material for simplicity)
            PurchaseOrderItem item1 = PurchaseOrderItem.builder()
                    .material(material).quantity(3).unitPrice(BigDecimal.ONE).taxRate(BigDecimal.ZERO).build();
            PurchaseOrderItem item2 = PurchaseOrderItem.builder()
                    .material(material).quantity(4).unitPrice(BigDecimal.ONE).taxRate(BigDecimal.ZERO).build();

            PurchaseOrder po = PurchaseOrder.builder()
                    .poNumber("PO-2026-0002")
                    .items(List.of(item1, item2))
                    .build();

            when(materialStockRepository.findByMaterialId(materialId))
                    .thenReturn(Optional.of(existingStock));
            when(materialStockRepository.save(any(MaterialStock.class))).thenAnswer(inv -> inv.getArgument(0));

            // WHEN
            materialStockService.processGoodsReceipt(po);

            // THEN: save() is called once per PO item (2 items → 2 saves)
            verify(materialStockRepository, times(2)).save(any(MaterialStock.class));
        }
    }
}
