package br.com.sifat.desafio.inventory_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class InventoryAdjustRequestDTO {
    @Schema(description = "Quantidade a ser ajustada (positiva para entrada, negativa para saída)", example = "-2")
    @NotNull(message = "A quantidade é obrigatória")
    private Integer quantity;

    @Schema(description = "Motivo do ajuste (Ex: 'VENDA', 'AJUSTE_MANUAL', 'RECEBIMENTO')", example = "VENDA_CLIENTE_X")
    private String reason;

    public InventoryAdjustRequestDTO() {
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
