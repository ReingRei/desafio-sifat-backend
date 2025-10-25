package br.com.sifat.desafio.inventory_service.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import br.com.sifat.desafio.inventory_service.event.ProductEventDTO;
import br.com.sifat.desafio.inventory_service.service.InventoryService;

@Service
public class KafkaConsumerService {

    private final InventoryService inventoryService;

    public KafkaConsumerService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = "product.events.v1", groupId = "${spring.kafka.consumer.group-id}")
    public void handleProductEvent(ProductEventDTO event) {
        System.out.println("EVENTO KAFKA RECEBIDO: " + event.getType() + " para o Produto ID: " + event.getProductId());
        inventoryService.processProductEvent(event);
    }
}