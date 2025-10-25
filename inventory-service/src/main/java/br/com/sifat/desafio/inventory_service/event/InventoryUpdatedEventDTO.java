package br.com.sifat.desafio.inventory_service.event;

import io.swagger.v3.oas.annotations.media.Schema;

public class InventoryUpdatedEventDTO {

    @Schema(description = "ID do produto cujo estoque foi alterado", example = "1")
    private Long productId;

    @Schema(description = "Nova quantidade em estoque", example = "105")
    private Integer newQuantity;

    public InventoryUpdatedEventDTO() {
    }

    public InventoryUpdatedEventDTO(Long productId, Integer newQuantity) {
        this.productId = productId;
        this.newQuantity = newQuantity;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(Integer newQuantity) {
        this.newQuantity = newQuantity;
    }
}