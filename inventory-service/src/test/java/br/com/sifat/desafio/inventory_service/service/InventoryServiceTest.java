package br.com.sifat.desafio.inventory_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import br.com.sifat.desafio.inventory_service.dto.InventoryAdjustRequestDTO;
import br.com.sifat.desafio.inventory_service.dto.InventoryResponseDTO;
import br.com.sifat.desafio.inventory_service.event.InventoryUpdatedEventDTO;
import br.com.sifat.desafio.inventory_service.event.ProductEventDTO;
import br.com.sifat.desafio.inventory_service.event.ProductEventDTO.EventType;
import br.com.sifat.desafio.inventory_service.mapper.InventoryMapper;
import br.com.sifat.desafio.inventory_service.model.Inventory;
import br.com.sifat.desafio.inventory_service.model.StockMovement;
import br.com.sifat.desafio.inventory_service.repository.InventoryRepository;
import br.com.sifat.desafio.inventory_service.repository.StockMovementRepository;
import jakarta.persistence.EntityNotFoundException;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do InventoryService")
public class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private InventoryMapper mapper;

    @Mock
    private KafkaTemplate<String, InventoryUpdatedEventDTO> kafkaTemplate;

    @InjectMocks
    private InventoryService inventoryService;

    @Nested
    @DisplayName("Testes para getInventoryByProductId")
    class GetInventoryByProductIdTests {

        @Test
        @DisplayName("Deve retornar o DTO do inventário quando o produto for encontrado")
        public void testGetInventory_WhenProductExists_shouldReturnInventoryDTO() {
            Long productId = 1L;

            Inventory inventoryEntity = new Inventory();
            inventoryEntity.setProductId(productId);
            inventoryEntity.setQuantity(50);

            InventoryResponseDTO responseDto = new InventoryResponseDTO();
            responseDto.setProductId(productId);
            responseDto.setQuantity(50);

            when(inventoryRepository.findById(productId)).thenReturn(Optional.of(inventoryEntity));

            when(mapper.toResponseDTO(inventoryEntity)).thenReturn(responseDto);

            InventoryResponseDTO result = inventoryService.getInventoryByProductId(productId);

            assertNotNull(result);
            assertEquals(productId, result.getProductId());
            assertEquals(50, result.getQuantity());

            verify(inventoryRepository, times(1)).findById(productId);
            verify(mapper, times(1)).toResponseDTO(inventoryEntity);
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException quando o produto não for encontrado")
        public void testGetInventory_WhenProductNotFound_shouldThrowEntityNotFoundException() {
            Long nonExistentId = 99L;

            when(inventoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
                inventoryService.getInventoryByProductId(nonExistentId);
            });

            assertTrue(exception.getMessage().contains("Inventário não encontrado"));

            verify(mapper, never()).toResponseDTO(any());
        }
    }

    @Nested
    @DisplayName("Testes para adjustStock")
    class AdjustStockTests {

        @Test
        @DisplayName("Deve atualizar o estoque e criar movimentação quando bem sucedido")
        public void testAdjustStock_WhenSuccessful_shouldUpdateInventoryAndCreateMovement() {
            Long productId = 1L;
            int quantityToAdjust = -5;
            String reason = "Venda Online";

            InventoryAdjustRequestDTO requestDto = new InventoryAdjustRequestDTO();
            requestDto.setQuantity(quantityToAdjust);
            requestDto.setReason(reason);

            Inventory updatedInventoryEntity = new Inventory();
            updatedInventoryEntity.setProductId(productId);
            updatedInventoryEntity.setQuantity(15);

            InventoryResponseDTO responseDto = new InventoryResponseDTO();
            responseDto.setProductId(productId);
            responseDto.setQuantity(15);

            when(inventoryRepository.adjustQuantity(productId, quantityToAdjust)).thenReturn(1);

            when(stockMovementRepository.save(any(StockMovement.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            when(inventoryRepository.findById(productId)).thenReturn(Optional.of(updatedInventoryEntity));

            when(mapper.toResponseDTO(updatedInventoryEntity)).thenReturn(responseDto);

            InventoryResponseDTO result = inventoryService.adjustStock(productId, requestDto);

            assertNotNull(result);
            assertEquals(productId, result.getProductId());
            assertEquals(15, result.getQuantity());

            verify(inventoryRepository, times(1)).adjustQuantity(productId, quantityToAdjust);
            verify(stockMovementRepository, times(1)).save(any(StockMovement.class));
            verify(inventoryRepository, times(1)).findById(productId);
            verify(mapper, times(1)).toResponseDTO(updatedInventoryEntity);
            verify(kafkaTemplate, times(1)).send(
                    anyString(),
                    anyString(),
                    any(InventoryUpdatedEventDTO.class));
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException quando o produto não for encontrado")
        public void testAdjustStock_WhenProductNotFound_shouldThrowEntityNotFoundException() {
            Long nonExistentId = 99L;
            InventoryAdjustRequestDTO requestDto = new InventoryAdjustRequestDTO();
            requestDto.setQuantity(5);
            requestDto.setReason("Entrada");

            when(inventoryRepository.adjustQuantity(nonExistentId, 5)).thenReturn(0);

            when(inventoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
                inventoryService.adjustStock(nonExistentId, requestDto);
            });

            assertTrue(exception.getMessage().contains("Inventário não encontrado"));

            verify(stockMovementRepository, never()).save(any(StockMovement.class));
            verify(mapper, never()).toResponseDTO(any());
        }

        @Test
        @DisplayName("Deve lançar DataIntegrityViolationException (ou similar) quando o ajuste resultar em estoque negativo")
        public void testAdjustStock_WhenAdjustmentLeadsToNegativeStock_shouldThrowException() {

            Long productId = 1L;
            int quantityToAdjust = -100;
            InventoryAdjustRequestDTO requestDto = new InventoryAdjustRequestDTO();
            requestDto.setQuantity(quantityToAdjust);
            requestDto.setReason("Erro de Venda");

            when(inventoryRepository.adjustQuantity(productId, quantityToAdjust))
                    .thenThrow(new DataIntegrityViolationException("CHECK constraint failed"));

            assertThrows(DataIntegrityViolationException.class, () -> {
                inventoryService.adjustStock(productId, requestDto);
            });

            verify(stockMovementRepository, never()).save(any(StockMovement.class));
            verify(inventoryRepository, never()).findById(anyLong());
            verify(mapper, never()).toResponseDTO(any());
        }
    }

    @Nested
    @DisplayName("Testes para processProductEvent (Eventos Kafka)")
    class ProcessProductEventTests {
        private Inventory inventoryMock = new Inventory();
        private Long productId = 1L;

        @BeforeEach
        void setUp() {
            inventoryMock.setProductId(productId);
            inventoryMock.setQuantity(0);
        }

        @Test
        @DisplayName("Deve criar o registro de Inventário e Movimentação ao receber evento CREATED")
        void testProcessEvent_WhenTypeIsCREATED_shouldCreateInventory() {

            ProductEventDTO event = new ProductEventDTO();
            event.setProductId(productId);
            event.setType(EventType.CREATED);

            when(inventoryRepository.findById(productId)).thenReturn(Optional.empty());

            when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventoryMock);

            inventoryService.processProductEvent(event);

            verify(inventoryRepository, times(1)).save(any(Inventory.class));

            verify(stockMovementRepository, times(1)).save(any(StockMovement.class));
        }

        @Test
        @DisplayName("Deve ignorar evento CREATED se o registro de Inventário já existir")
        void testProcessEvent_WhenTypeIsCREATED_AndInventoryExists_shouldIgnoreSave() {

            ProductEventDTO event = new ProductEventDTO();
            event.setProductId(productId);
            event.setType(EventType.CREATED);

            when(inventoryRepository.findById(productId)).thenReturn(Optional.of(inventoryMock));

            inventoryService.processProductEvent(event);

            verify(inventoryRepository, never()).save(any(Inventory.class));

            verify(stockMovementRepository, never()).save(any(StockMovement.class));
        }

        @Test
        @DisplayName("Deve realizar o Soft Delete no Inventário ao receber evento DELETED")
        void testProcessEvent_WhenTypeIsDELETED_shouldCallDeleteById() {

            ProductEventDTO event = new ProductEventDTO();
            event.setProductId(productId);
            event.setType(EventType.DELETED);

            when(inventoryRepository.existsById(productId)).thenReturn(true);

            inventoryService.processProductEvent(event);

            verify(inventoryRepository, times(1)).deleteById(productId);

            verify(stockMovementRepository, times(1)).save(any(StockMovement.class));
        }

        @Test
        @DisplayName("Deve ignorar evento DELETED se o registro de Inventário não existir")
        void testProcessEvent_WhenTypeIsDELETED_AndInventoryNotExists_shouldNotAttemptDelete() {

            ProductEventDTO event = new ProductEventDTO();
            event.setProductId(productId);
            event.setType(EventType.DELETED);

            when(inventoryRepository.existsById(productId)).thenReturn(false);

            inventoryService.processProductEvent(event);

            verify(inventoryRepository, never()).deleteById(anyLong());

            verify(stockMovementRepository, never()).save(any(StockMovement.class));
        }

        @Test
        @DisplayName("Deve ignorar evento UPDATED (não há lógica de estoque para updates)")
        void testProcessEvent_WhenTypeIsUPDATED_shouldDoNothing() {

            ProductEventDTO event = new ProductEventDTO();
            event.setProductId(productId);
            event.setType(EventType.UPDATED);

            inventoryService.processProductEvent(event);

            verify(inventoryRepository, never()).save(any(Inventory.class));
            verify(inventoryRepository, never()).deleteById(anyLong());
            verify(stockMovementRepository, never()).save(any(StockMovement.class));
        }
    }
}