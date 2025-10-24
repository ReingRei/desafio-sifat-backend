package br.com.sifat.desafio.product_service.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import br.com.sifat.desafio.product_service.dto.CategoryDTO;
import br.com.sifat.desafio.product_service.dto.ProductRequestDTO;
import br.com.sifat.desafio.product_service.dto.ProductResponseDTO;
import br.com.sifat.desafio.product_service.model.Category;
import br.com.sifat.desafio.product_service.model.Product;

@Component
public class ProductMapper {
    private static final BigDecimal CENTS_CONVERTER = new BigDecimal(100);

    public Product toEntity(ProductRequestDTO dto, Category category) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setImageUrl(dto.getImageUrl());
        product.setCategory(category);
        product.setPrice(dto.getPrice().multiply(CENTS_CONVERTER).longValue());
        return product;
    }

    public ProductResponseDTO toResponseDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setImageUrl(product.getImageUrl());
        dto.setCreatedAt(product.getCreatedAt());

         dto.setPrice(new BigDecimal(product.getPrice())
                .divide(CENTS_CONVERTER, 2, RoundingMode.HALF_UP));
        
        if (product.getCategory() != null) {
            dto.setCategory(toCategoryDTO(product.getCategory()));
        }
        return dto;

    }

    public CategoryDTO toCategoryDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
}
