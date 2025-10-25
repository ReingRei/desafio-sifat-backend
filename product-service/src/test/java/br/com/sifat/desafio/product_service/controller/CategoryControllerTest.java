package br.com.sifat.desafio.product_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.sifat.desafio.product_service.dto.CategoryDTO;
import br.com.sifat.desafio.product_service.service.CategoryService;
import java.util.List;

@WebMvcTest(CategoryController.class)
public class CategoryControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private CategoryService categoryService;

        @Nested
        @DisplayName("Testes para GET /category (Buscar categorias)")
        class getAllCategoriesTests {
                @Test
                @DisplayName("Deve retornar 200 OK com a lista de categorias quando houver dados")
                public void testFindAll_WhenCategoriesExist_shouldReturn200OkAndCategoryList() throws Exception {

                        CategoryDTO dto1 = new CategoryDTO();
                        dto1.setId(1L);
                        dto1.setName("Eletrônicos");

                        CategoryDTO dto2 = new CategoryDTO();
                        dto2.setId(2L);
                        dto2.setName("Roupas");

                        List<CategoryDTO> expectedList = List.of(dto1, dto2);

                        when(categoryService.getAllCategories()).thenReturn(expectedList);

                        mockMvc.perform(
                                        get("/categories")
                                                        .accept(MediaType.APPLICATION_JSON))
                                        .andExpect(
                                                        status().isOk())
                                        .andExpect(
                                                        content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(
                                                        jsonPath("$[0].id").value(1L))
                                        .andExpect(
                                                        jsonPath("$[0].name").value("Eletrônicos"))
                                        .andExpect(
                                                        jsonPath("$[1].id").value(2L))
                                        .andExpect(
                                                        jsonPath("$[1].name").value("Roupas"));

                        verify(categoryService, times(1)).getAllCategories();
                }
        }
}