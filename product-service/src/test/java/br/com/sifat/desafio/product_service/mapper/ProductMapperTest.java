package br.com.sifat.desafio.product_service.mapper;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import br.com.sifat.desafio.product_service.dto.ProductRequestDTO;
import br.com.sifat.desafio.product_service.dto.ProductResponseDTO;
import br.com.sifat.desafio.product_service.model.Product;
import br.com.sifat.desafio.product_service.model.Category;
import java.math.BigDecimal;

public class ProductMapperTest {
    private ProductMapper mapper = new ProductMapper();

    @Test
    public void testToEntity_shouldConvertPriceToCents() {
        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setName("Teste");
        dto.setPrice(new BigDecimal("199.99"));
        dto.setCategoryId(1L);

        Category category = new Category();
        category.setId(1L);

        Product product = mapper.toEntity(dto, category);


        assertNotNull(product);
        assertEquals("Teste", product.getName());
        assertEquals(19999L, product.getPrice());
    }

    @Test
    public void testToResponseDTO_shouldConvertPriceFromCents() {
        
        Product product = new Product();
        product.setId(1L);
        product.setName("Teste");
        product.setPrice(19999L);
        
        Category category = new Category();
        category.setId(1L);
        category.setName("Eletrônicos");
        product.setCategory(category);
        
        
        ProductResponseDTO dto = mapper.toResponseDTO(product);
        
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Eletrônicos", dto.getCategory().getName());
        
        assertEquals(0, new BigDecimal("199.99").compareTo(dto.getPrice()));
    }
}
