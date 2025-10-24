package br.com.sifat.desafio.product_service.repository.specification;

import br.com.sifat.desafio.product_service.model.Category;
import br.com.sifat.desafio.product_service.model.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    private static final BigDecimal CENTS_CONVERTER = new BigDecimal(100);

    public static Specification<Product> filterBy(String name, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(name)) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (categoryId != null) {
                Join<Product, Category> categoryJoin = root.join("category");
                predicates.add(cb.equal(categoryJoin.get("id"), categoryId));
            }

            if (minPrice != null) {
                Long minPriceInCents = minPrice.multiply(CENTS_CONVERTER).longValue();
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPriceInCents));
            }

            if (maxPrice != null) {
                Long maxPriceInCents = maxPrice.multiply(CENTS_CONVERTER).longValue();
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPriceInCents));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}