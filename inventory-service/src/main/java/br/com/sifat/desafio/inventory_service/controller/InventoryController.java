package br.com.sifat.desafio.inventory_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sifat.desafio.inventory_service.dto.InventoryAdjustRequestDTO;
import br.com.sifat.desafio.inventory_service.dto.InventoryResponseDTO;
import br.com.sifat.desafio.inventory_service.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/inventory")
@Tag(name = "Inventário", description = "Endpoints para consulta e ajuste de estoque")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Busca o estoque de um produto por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estoque encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Inventário não encontrado para o produto")
    })
    public ResponseEntity<InventoryResponseDTO> getInventory(
            @PathVariable Long productId) {

        InventoryResponseDTO responseDto = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/{productId}/adjust")
    @Operation(summary = "Ajusta o estoque de um produto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estoque ajustado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (ex: dados faltando, estoque ficaria negativo)"),
            @ApiResponse(responseCode = "404", description = "Inventário não encontrado para o produto")
    })
    public ResponseEntity<InventoryResponseDTO> adjustInventory(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryAdjustRequestDTO requestDto) {

        InventoryResponseDTO updatedDto = inventoryService.adjustStock(productId, requestDto);
        return ResponseEntity.ok(updatedDto);
    }
}
