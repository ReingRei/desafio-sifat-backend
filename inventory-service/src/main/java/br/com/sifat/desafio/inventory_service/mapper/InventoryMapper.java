package br.com.sifat.desafio.inventory_service.mapper;

import org.springframework.stereotype.Component;

import br.com.sifat.desafio.inventory_service.dto.InventoryResponseDTO;
import br.com.sifat.desafio.inventory_service.model.Inventory;

@Component
public class InventoryMapper {
    public InventoryResponseDTO toResponseDTO(Inventory inventory) {
        if (inventory == null) {
            return null;
        }

        InventoryResponseDTO dto = new InventoryResponseDTO();

        dto.setProductId(inventory.getProductId());
        dto.setQuantity(inventory.getQuantity());

        return dto;
    }
}
