package br.com.sifat.desafio.inventory_service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sifat.desafio.inventory_service.dto.InventoryAdjustRequestDTO;
import br.com.sifat.desafio.inventory_service.dto.InventoryResponseDTO;
import br.com.sifat.desafio.inventory_service.mapper.InventoryMapper;
import br.com.sifat.desafio.inventory_service.model.Inventory;
import br.com.sifat.desafio.inventory_service.model.StockMovement;
import br.com.sifat.desafio.inventory_service.repository.InventoryRepository;
import br.com.sifat.desafio.inventory_service.repository.StockMovementRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InventoryMapper mapper;

    public InventoryService(InventoryRepository inventoryRepository,
            StockMovementRepository stockMovementRepository,
            InventoryMapper mapper) {
        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.mapper = mapper;
    }

    private Inventory findInventoryById(Long productId) {
        return inventoryRepository.findById(productId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Inventário não encontrado."));
    }

    @Transactional(readOnly = true)
    public InventoryResponseDTO getInventoryByProductId(Long productId) {
        Inventory inventory = findInventoryById(productId);
        return mapper.toResponseDTO(inventory);
    }

    @Transactional
    public InventoryResponseDTO adjustStock(Long productId, InventoryAdjustRequestDTO requestDto) {

        int quantityToAdjust = requestDto.getQuantity();
        String reason = requestDto.getReason();

        int rowsAffected = inventoryRepository.adjustQuantity(productId, quantityToAdjust);

        if (rowsAffected == 0) {
            findInventoryById(productId);
             throw new IllegalStateException("Falha ao atualizar inventário");
        }

        StockMovement movement = new StockMovement();
        movement.setProductId(productId);
        movement.setQuantityChanged(quantityToAdjust);
        movement.setReason(reason);
        stockMovementRepository.save(movement);

        Inventory currentInventory = findInventoryById(productId);

        // TODO: Publicar evento Kafka 'inventory.updated'

        return mapper.toResponseDTO(currentInventory);

    }

    // TODO: Adicionar métodos que serão chamados pelo Consumidor Kafka
}
