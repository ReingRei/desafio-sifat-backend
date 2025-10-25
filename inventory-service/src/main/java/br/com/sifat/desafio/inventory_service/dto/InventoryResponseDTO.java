package br.com.sifat.desafio.inventory_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class InventoryResponseDTO {
    @Schema(description = "ID do produto", example = "1")
    private Long productId;

    @Schema(description = "Quantidade atual em estoque", example = "15")
    private Integer quantity;

    public InventoryResponseDTO() {
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

}
