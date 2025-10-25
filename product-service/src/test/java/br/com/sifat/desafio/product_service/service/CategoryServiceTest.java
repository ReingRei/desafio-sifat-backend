package br.com.sifat.desafio.product_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.sifat.desafio.product_service.dto.CategoryDTO;
import br.com.sifat.desafio.product_service.mapper.ProductMapper;
import br.com.sifat.desafio.product_service.model.Category;
import br.com.sifat.desafio.product_service.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private CategoryService categoryService;

    @Nested
    @DisplayName("getAllCategories Tests")
    class GetAllCategoriesTests {
        @Test
        @DisplayName("Retornar uma lista de CategoryDTO quando houver dados")
        public void testFindAll_WhenCategoriesExist_shouldReturnCategoryDTOList() {
            Category category1 = new Category();
            category1.setId(1L);
            category1.setName("Eletrônicos");

            Category category2 = new Category();
            category2.setId(2L);
            category2.setName("Roupas");

            List<Category> categoryList = List.of(category1, category2);

            CategoryDTO dto1 = new CategoryDTO();
            dto1.setId(1L);
            dto1.setName("Eletrônicos");

            CategoryDTO dto2 = new CategoryDTO();
            dto2.setId(2L);
            dto2.setName("Roupas");

            when(categoryRepository.findAll()).thenReturn(categoryList);

            when(productMapper.toCategoryDTO(category1)).thenReturn(dto1);
            when(productMapper.toCategoryDTO(category2)).thenReturn(dto2);

            List<CategoryDTO> result = categoryService.getAllCategories();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Eletrônicos", result.get(0).getName());
            assertEquals("Roupas", result.get(1).getName());

            verify(categoryRepository, times(1)).findAll();

            verify(productMapper, times(1)).toCategoryDTO(category1);
            verify(productMapper, times(1)).toCategoryDTO(category2);
        }
    }
}
