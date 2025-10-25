package br.com.sifat.desafio.inventory_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.sifat.desafio.inventory_service.dto.InventoryAdjustRequestDTO;
import br.com.sifat.desafio.inventory_service.dto.InventoryResponseDTO;
import br.com.sifat.desafio.inventory_service.service.InventoryService;
import jakarta.persistence.EntityNotFoundException;
import java.lang.IllegalArgumentException;

@WebMvcTest(InventoryController.class)
@DisplayName("Testes do InventoryController")
public class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService inventoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("Testes para GET /inventory/{productId} (GetInventory)")
    class GetInventoryTests {

        @Test
        @DisplayName("Deve retornar 200 OK com o corpo do inventário quando encontrado")
        public void testGetInventory_WhenExists_shouldReturn200OkAndBody() throws Exception {
            Long productId = 1L;
            InventoryResponseDTO responseDto = new InventoryResponseDTO();
            responseDto.setProductId(productId);
            responseDto.setQuantity(100);

            when(inventoryService.getInventoryByProductId(productId)).thenReturn(responseDto);

            mockMvc.perform(
                    get("/inventory/{productId}", productId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.productId").value(productId))
                    .andExpect(jsonPath("$.quantity").value(100));

            verify(inventoryService, times(1)).getInventoryByProductId(productId);
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando o inventário não for encontrado")
        public void testGetInventory_WhenNotFound_shouldReturn404NotFound() throws Exception {
            Long nonExistentId = 99L;
            String errorMessage = "Inventário não encontrado";

            when(inventoryService.getInventoryByProductId(nonExistentId))
                    .thenThrow(new EntityNotFoundException(errorMessage));

            mockMvc.perform(
                    get("/inventory/{productId}", nonExistentId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(errorMessage));

            verify(inventoryService, times(1)).getInventoryByProductId(nonExistentId);
        }
    }

    @Nested
    @DisplayName("Testes para PATCH /inventory/{productId}/adjust (AdjustInventory)")
    class AdjustInventoryTests {

        @Test
        @DisplayName("Deve retornar 200 OK com o corpo atualizado quando bem sucedido")
        public void testAdjustInventory_WhenSuccessful_shouldReturn200OkAndUpdatedBody() throws Exception {
            Long productId = 1L;
            InventoryAdjustRequestDTO requestDto = new InventoryAdjustRequestDTO();
            requestDto.setQuantity(-10);
            requestDto.setReason("Venda");

            InventoryResponseDTO responseDto = new InventoryResponseDTO();
            responseDto.setProductId(productId);
            responseDto.setQuantity(90);

            when(inventoryService.adjustStock(eq(productId), any(InventoryAdjustRequestDTO.class)))
                    .thenReturn(responseDto);

            mockMvc.perform(
                    patch("/inventory/{productId}/adjust", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.productId").value(productId))
                    .andExpect(jsonPath("$.quantity").value(90));

            verify(inventoryService, times(1)).adjustStock(eq(productId), any(InventoryAdjustRequestDTO.class));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando o inventário do produto não for encontrado")
        public void testAdjustInventory_WhenProductNotFound_shouldReturn404NotFound() throws Exception {
            Long nonExistentId = 99L;
            InventoryAdjustRequestDTO requestDto = new InventoryAdjustRequestDTO();
            requestDto.setQuantity(5);
            String errorMessage = "Inventário não encontrado";

            when(inventoryService.adjustStock(eq(nonExistentId), any(InventoryAdjustRequestDTO.class)))
                    .thenThrow(new EntityNotFoundException(errorMessage));

            mockMvc.perform(
                    patch("/inventory/{productId}/adjust", nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(errorMessage));

            verify(inventoryService, times(1)).adjustStock(eq(nonExistentId), any(InventoryAdjustRequestDTO.class));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request quando a requisição for inválida (ex: quantidade nula)")
        public void testAdjustInventory_WhenInvalidRequest_shouldReturn400BadRequest() throws Exception {
            Long productId = 1L;
            InventoryAdjustRequestDTO invalidRequestDto = new InventoryAdjustRequestDTO();
            invalidRequestDto.setQuantity(null);
            invalidRequestDto.setReason("Teste");

            mockMvc.perform(
                    patch("/inventory/{productId}/adjust", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Erro de Validação"))
                    .andExpect(jsonPath("$.messages.quantity").value("A quantidade é obrigatória"));

            verify(inventoryService, never()).adjustStock(anyLong(), any());
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request quando o ajuste resultar em erro (ex: estoque negativo)")
        public void testAdjustInventory_WhenAdjustmentCausesError_shouldReturn400BadRequest() throws Exception {
            Long productId = 1L;
            InventoryAdjustRequestDTO requestDto = new InventoryAdjustRequestDTO();
            requestDto.setQuantity(-1000);
            String errorMessage = "Estoque insuficiente";

            when(inventoryService.adjustStock(eq(productId), any(InventoryAdjustRequestDTO.class)))
                    .thenThrow(new IllegalArgumentException(errorMessage));

            mockMvc.perform(
                    patch("/inventory/{productId}/adjust", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Requisição Inválida"))
                    .andExpect(jsonPath("$.message").value(errorMessage));

            verify(inventoryService, times(1)).adjustStock(eq(productId), any(InventoryAdjustRequestDTO.class));
        }
    }
}