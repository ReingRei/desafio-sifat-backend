package br.com.sifat.desafio.inventory_service.service;

import java.util.Optional;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sifat.desafio.inventory_service.dto.InventoryAdjustRequestDTO;
import br.com.sifat.desafio.inventory_service.dto.InventoryResponseDTO;
import br.com.sifat.desafio.inventory_service.event.InventoryUpdatedEventDTO;
import br.com.sifat.desafio.inventory_service.event.ProductEventDTO;
import br.com.sifat.desafio.inventory_service.mapper.InventoryMapper;
import br.com.sifat.desafio.inventory_service.model.Inventory;
import br.com.sifat.desafio.inventory_service.model.StockMovement;
import br.com.sifat.desafio.inventory_service.repository.InventoryRepository;
import br.com.sifat.desafio.inventory_service.repository.StockMovementRepository;
import br.com.sifat.desafio.inventory_service.event.ProductEventDTO.EventType;
import jakarta.persistence.EntityNotFoundException;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InventoryMapper mapper;
    private final KafkaTemplate<String, InventoryUpdatedEventDTO> kafkaTemplate;

    public InventoryService(InventoryRepository inventoryRepository,
            StockMovementRepository stockMovementRepository,
            InventoryMapper mapper,
            KafkaTemplate<String, InventoryUpdatedEventDTO> kafkaTemplate) {
        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.mapper = mapper;
        this.kafkaTemplate = kafkaTemplate;
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

        try {
            InventoryUpdatedEventDTO event = new InventoryUpdatedEventDTO(
                    currentInventory.getProductId(),
                    currentInventory.getQuantity());
            kafkaTemplate.send("inventory.updated.v1", productId.toString(), event);
        } catch (Exception e) {
            System.err.println("ERRO KAFKA (BÔNUS): Falha ao notificar atualização de estoque para ID " + productId
                    + " - " + e.getMessage());
        }

        return mapper.toResponseDTO(currentInventory);

    }

    @Transactional
    public void processProductEvent(ProductEventDTO event) {
        Long productId = event.getProductId();
        EventType type = event.getType();

        switch (type) {
            case CREATED:
                handleProductCreated(productId);
                break;
            case UPDATED:
                System.out.println("INVENTORY: Evento UPDATED recebido para ID " + productId
                        + " (Nenhuma ação no estoque necessária)");
                break;
            case DELETED:
                handleProductDeleted(productId);
                break;
            default:
                System.err.println("Tipo de evento desconhecido: " + type);
        }
    }

    private void handleProductCreated(Long productId) {
        Optional<Inventory> existingInventory = inventoryRepository.findById(productId);

        if (existingInventory.isPresent()) {
            System.out.println("INVENTORY: Produto ID " + productId + " já existe. Ignorando evento CREATED.");
            return;
        }

        Inventory newInventory = new Inventory();
        newInventory.setProductId(productId);
        newInventory.setQuantity(0);

        inventoryRepository.save(newInventory);

        StockMovement movement = new StockMovement();
        movement.setProductId(productId);
        movement.setQuantityChanged(0);
        movement.setReason("PRODUTO_CRIADO");
        stockMovementRepository.save(movement);

        System.out
                .println("INVENTORY: Novo inventário criado para Produto ID " + productId + " com estoque inicial 0.");
    }

    private void handleProductDeleted(Long productId) {

        if (inventoryRepository.existsById(productId)) {
            inventoryRepository.deleteById(productId);

            StockMovement movement = new StockMovement();
            movement.setProductId(productId);
            movement.setQuantityChanged(0);
            movement.setReason("PRODUTO_DELETADO_SISTEMA");
            stockMovementRepository.save(movement);

            System.out.println(
                    "INVENTORY: Inventário de Produto ID " + productId + " foi marcado como DELETADO (Soft Delete).");
        } else {
            System.out.println("INVENTORY: Produto ID " + productId + " não encontrado para exclusão. Ignorando.");
        }
    }
}
