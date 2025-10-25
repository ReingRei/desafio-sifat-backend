package br.com.sifat.desafio.inventory_service.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory")
@SQLDelete(sql = "UPDATE inventory SET deleted_at = CURRENT_TIMESTAMP WHERE product_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Inventory {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Inventory() {
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

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
