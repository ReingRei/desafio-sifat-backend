package br.com.sifat.desafio.inventory_service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import br.com.sifat.desafio.inventory_service.dto.InventoryResponseDTO;
import br.com.sifat.desafio.inventory_service.model.Inventory;

public class InventoryMapperTest {

    private InventoryMapper inventoryMapper = new InventoryMapper();

    @Nested
    @DisplayName("toResponseDTO tests")
    class toResponseDTOTests {

        @Test
        @DisplayName("Retornar o DTO corretamente quando a entidade de inventário existir")
        public void testToResponseDTO_WhenInventoryExists_shouldMapCorrectly() {
            Inventory inventoryEntity = new Inventory();
            inventoryEntity.setProductId(1L);
            inventoryEntity.setQuantity(25);

            InventoryResponseDTO resultDto = inventoryMapper.toResponseDTO(inventoryEntity);

            assertNotNull(resultDto);
            assertEquals(1L, resultDto.getProductId());
            assertEquals(25, resultDto.getQuantity());
        }

        @Test
        @DisplayName("Retornar null quando a entidade de inventário for null")
        public void testToResponseDTO_WhenInventoryIsNull_shouldReturnNull() {

            Inventory inventoryEntity = null;

            InventoryResponseDTO resultDto = inventoryMapper.toResponseDTO(inventoryEntity);

            assertNull(resultDto);
        }
    }
}
