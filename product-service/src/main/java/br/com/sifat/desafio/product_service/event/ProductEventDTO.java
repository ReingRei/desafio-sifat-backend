package br.com.sifat.desafio.product_service.event;

import java.math.BigDecimal;

public class ProductEventDTO {

    private EventType type;
    private Long productId;
    private String name;
    private BigDecimal price;
    private Long categoryId;

    public enum EventType {
        CREATED, UPDATED, DELETED
    }

    public ProductEventDTO() {
    }

    public ProductEventDTO(EventType type, Long productId, String name, BigDecimal price, Long categoryId) {
        this.type = type;
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.categoryId = categoryId;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}