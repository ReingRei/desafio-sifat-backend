package br.com.sifat.desafio.inventory_service.repository;

import br.com.sifat.desafio.inventory_service.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
}
