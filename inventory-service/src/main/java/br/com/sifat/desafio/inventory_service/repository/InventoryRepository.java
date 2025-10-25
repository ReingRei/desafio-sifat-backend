package br.com.sifat.desafio.inventory_service.repository;

import br.com.sifat.desafio.inventory_service.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Atualiza atomicamente a quantidade de um produto.
     * A validação de não-negativo é feita pela CHECK constraint no banco.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.quantity = i.quantity + :amount WHERE i.productId = :productId")
    int adjustQuantity(@Param("productId") Long productId, @Param("amount") int amount);
}