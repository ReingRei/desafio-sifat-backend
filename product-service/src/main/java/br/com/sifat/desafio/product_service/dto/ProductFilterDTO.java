package br.com.sifat.desafio.product_service.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

public class ProductFilterDTO {

    @Schema(description = "Filtrar por nome (parcial)", example = "Teclado")
    private String name;

    @Schema(description = "Filtrar por ID da categoria", example = "1")
    private Long categoryId;

    @Schema(description = "Filtrar por preço mínimo", example = "50.00")
    private BigDecimal minPrice;

    @Schema(description = "Filtrar por preço máximo", example = "300.00")
    private BigDecimal maxPrice;

    public ProductFilterDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }
}
