package br.com.sifat.desafio.product_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.TypeInformation;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.sifat.desafio.product_service.dto.ProductFilterDTO;
import br.com.sifat.desafio.product_service.dto.ProductRequestDTO;
import br.com.sifat.desafio.product_service.dto.ProductResponseDTO;
import br.com.sifat.desafio.product_service.model.Product;
import br.com.sifat.desafio.product_service.service.ProductService;
import jakarta.persistence.EntityNotFoundException;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {
        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private ProductService productService;

        @Autowired
        private ObjectMapper objectMapper;

        @Nested
        @DisplayName("Testes para POST /products (Criar Produto)")
        class CreateProductTests {
                @Test
                @DisplayName("Deve retornar 201 Created com Location e Body quando bem sucedido")
                public void testCreateProduct_WhenSuccessful_shouldReturn201CreatedWithLocationAndBody()
                                throws Exception {
                        ProductRequestDTO requestDto = new ProductRequestDTO();
                        requestDto.setName("Monitor Gamer");
                        requestDto.setPrice(new BigDecimal("1200.50"));
                        requestDto.setCategoryId(1L);

                        ProductResponseDTO responseDto = new ProductResponseDTO();
                        responseDto.setId(5L);
                        responseDto.setName("Monitor Gamer");
                        responseDto.setPrice(new BigDecimal("1200.50"));

                        when(productService.createProduct(any(ProductRequestDTO.class)))
                                        .thenReturn(responseDto);

                        mockMvc.perform(
                                        post("/products")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(requestDto)))
                                        .andExpect(
                                                        status().isCreated())
                                        .andExpect(
                                                        header().string("Location", "http://localhost/products/5"))
                                        .andExpect(
                                                        jsonPath("$.id").value(5L))
                                        .andExpect(
                                                        jsonPath("$.name").value("Monitor Gamer"))
                                        .andExpect(
                                                        jsonPath("$.price").value(1200.50));

                        verify(productService, times(1)).createProduct(any(ProductRequestDTO.class));
                }

                @Test
                @DisplayName("Deve retornar 400 Bad Request quando a requisição for inválida")
                public void testCreateProduct_WhenInvalidRequest_shouldReturn400BadRequest() throws Exception {
                        ProductRequestDTO invalidRequestDto = new ProductRequestDTO();
                        invalidRequestDto.setName("");
                        invalidRequestDto.setPrice(new BigDecimal("150.00"));
                        invalidRequestDto.setCategoryId(1L);

                        mockMvc.perform(
                                        post("/products")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(invalidRequestDto)))
                                        .andExpect(
                                                        status().isBadRequest())
                                        .andExpect(
                                                        jsonPath("$.error").value("Erro de Validação"))
                                        .andExpect(
                                                        jsonPath("$.messages.name").value("O nome é obrigatório"));

                        verify(productService, never()).createProduct(any());
                }
        }

        @Nested
        @DisplayName("Testes para GET /products/{id} (Buscar por ID)")
        class GetProductByIdTests {
                @Test
                @DisplayName("Deve retornar 200 OK com o corpo do produto quando encontrado")
                public void testGetProductById_WhenProductExists_shouldReturn200OkAndProductBody() throws Exception {
                        Long productId = 1L;

                        ProductResponseDTO responseDto = new ProductResponseDTO();
                        responseDto.setId(productId);
                        responseDto.setName("Produto Encontrado");
                        responseDto.setPrice(new BigDecimal("99.90"));

                        when(productService.getProductById(productId)).thenReturn(responseDto);

                        mockMvc.perform(
                                        get("/products/{id}", productId)
                                                        .accept(MediaType.APPLICATION_JSON))
                                        .andExpect(
                                                        status().isOk())
                                        .andExpect(
                                                        content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(
                                                        jsonPath("$.id").value(productId))
                                        .andExpect(
                                                        jsonPath("$.name").value("Produto Encontrado"))
                                        .andExpect(
                                                        jsonPath("$.price").value(99.90));

                        verify(productService, times(1)).getProductById(productId);
                }

                @Test
                @DisplayName("Deve retornar 404 Not Found quando o produto não for encontrado")
                public void testGetProductById_WhenProductNotFound_shouldReturn404NotFound() throws Exception {
                        Long nonExistentId = 99L;

                        String expectedErrorMessage = "Produto não encontrado.";

                        when(productService.getProductById(nonExistentId))
                                        .thenThrow(new EntityNotFoundException(expectedErrorMessage));

                        mockMvc.perform(
                                        get("/products/{id}", nonExistentId)
                                                        .accept(MediaType.APPLICATION_JSON))
                                        .andExpect(
                                                        status().isNotFound())
                                        .andExpect(
                                                        content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(
                                                        jsonPath("$.status").value(404))
                                        .andExpect(
                                                        jsonPath("$.error").value("Não Encontrado"))
                                        .andExpect(
                                                        jsonPath("$.message").value(expectedErrorMessage));

                        verify(productService, times(1)).getProductById(nonExistentId);
                }
        }

        @Nested
        @DisplayName("Testes para PUT /products/{id} (Atualizar Produto)")
        class UpdateProductTests {
                @Test
                @DisplayName("Deve retornar 200 OK com o corpo atualizado quando bem sucedido")
                public void testUpdateProduct_WhenSuccessful_shouldReturn200OkAndUpdatedBody() throws Exception {

                        Long productId = 1L;

                        ProductRequestDTO requestDto = new ProductRequestDTO();
                        requestDto.setName("Nome Atualizado");
                        requestDto.setPrice(new BigDecimal("250.75"));
                        requestDto.setCategoryId(2L);

                        ProductResponseDTO responseDto = new ProductResponseDTO();
                        responseDto.setId(productId);
                        responseDto.setName("Nome Atualizado");
                        responseDto.setPrice(new BigDecimal("250.75"));

                        when(productService.updateProduct(eq(productId), any(ProductRequestDTO.class)))
                                        .thenReturn(responseDto);

                        mockMvc.perform(
                                        put("/products/{id}", productId)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(requestDto)))
                                        .andExpect(
                                                        status().isOk())
                                        .andExpect(
                                                        content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(
                                                        jsonPath("$.id").value(productId))
                                        .andExpect(
                                                        jsonPath("$.name").value("Nome Atualizado"))
                                        .andExpect(
                                                        jsonPath("$.price").value(250.75));

                        verify(productService, times(1)).updateProduct(eq(productId), any(ProductRequestDTO.class));
                }

                @Test
                @DisplayName("Deve retornar 400 Bad Request quando a requisição for inválida")
                public void testUpdateProduct_WhenInvalidRequest_shouldReturn400BadRequest() throws Exception {
                        Long productId = 1L;

                        ProductRequestDTO invalidRequestDto = new ProductRequestDTO();
                        invalidRequestDto.setName("Nome Valido");
                        invalidRequestDto.setPrice(new BigDecimal("-50.00"));
                        invalidRequestDto.setCategoryId(1L);

                        mockMvc.perform(
                                        put("/products/{id}", productId)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(invalidRequestDto)))
                                        .andExpect(
                                                        status().isBadRequest())
                                        .andExpect(
                                                        jsonPath("$.error").value("Erro de Validação"))
                                        .andExpect(
                                                        jsonPath("$.messages.price")
                                                                        .value("O preço deve ser positivo"));

                        verify(productService, never()).updateProduct(anyLong(), any());
                }

                @Test
                @DisplayName("Deve retornar 404 Not Found quando o produto não for encontrado")
                public void testUpdateProduct_WhenProductNotFound_shouldReturn404NotFound() throws Exception {
                        Long nonExistentId = 99L;

                        ProductRequestDTO requestDto = new ProductRequestDTO();
                        requestDto.setName("Qualquer Nome");
                        requestDto.setPrice(new BigDecimal("10.00"));
                        requestDto.setCategoryId(1L);

                        String expectedErrorMessage = "Produto não encontrado com ID: " + nonExistentId;

                        when(productService.updateProduct(eq(nonExistentId), any(ProductRequestDTO.class)))
                                        .thenThrow(new EntityNotFoundException(expectedErrorMessage));

                        mockMvc.perform(
                                        put("/products/{id}", nonExistentId)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(requestDto)))
                                        .andExpect(
                                                        status().isNotFound())
                                        .andExpect(
                                                        jsonPath("$.status").value(404))
                                        .andExpect(
                                                        jsonPath("$.error").value("Não Encontrado"))
                                        .andExpect(
                                                        jsonPath("$.message").value(expectedErrorMessage));

                        verify(productService, times(1)).updateProduct(eq(nonExistentId), any(ProductRequestDTO.class));
                }
        }

        @Nested
        @DisplayName("Testes para DELETE /products/{id} (Deletar Produto)")
        class DeleteProductTests {
                @Test
                @DisplayName("Deve retornar 204 No Content quando bem sucedido")
                public void testDeleteProduct_WhenSuccessful_shouldReturn204NoContent() throws Exception {
                        Long productId = 1L;

                        doNothing().when(productService).deleteProduct(productId);

                        mockMvc.perform(
                                        delete("/products/{id}", productId))
                                        .andExpect(
                                                        status().isNoContent());

                        verify(productService, times(1)).deleteProduct(productId);
                }

                @Test
                @DisplayName("Deve retornar 404 Not Found quando o produto não for encontrado")
                public void testDeleteProduct_WhenProductNotFound_shouldReturn404NotFound() throws Exception {
                        Long nonExistentId = 99L;

                        String expectedErrorMessage = "Produto não encontrado com ID: " + nonExistentId;

                        doThrow(new EntityNotFoundException(expectedErrorMessage))
                                        .when(productService).deleteProduct(nonExistentId);

                        mockMvc.perform(
                                        delete("/products/{id}", nonExistentId))
                                        .andExpect(
                                                        status().isNotFound())
                                        .andExpect(
                                                        jsonPath("$.status").value(404))
                                        .andExpect(
                                                        jsonPath("$.error").value("Não Encontrado"))
                                        .andExpect(
                                                        jsonPath("$.message").value(expectedErrorMessage));

                        verify(productService, times(1)).deleteProduct(nonExistentId);
                }
        }

        @Nested
        @DisplayName("Testes para GET /products (Listar/Paginar)")
        class GetAllProductsPaginatedTests {
                @Test
                @DisplayName("Deve retornar 200 OK com a página de produtos quando houver dados")
                public void testGetAllProductsPaginated_WhenDataExists_shouldReturn200OkAndPageBody() throws Exception {
                        ProductFilterDTO filter = new ProductFilterDTO();
                        Pageable pageable = PageRequest.of(0, 2);

                        ProductResponseDTO dto1 = new ProductResponseDTO();
                        dto1.setId(1L);
                        dto1.setName("Produto A");
                        dto1.setPrice(new BigDecimal("10.00"));

                        ProductResponseDTO dto2 = new ProductResponseDTO();
                        dto2.setId(2L);
                        dto2.setName("Produto B");
                        dto2.setPrice(new BigDecimal("20.00"));

                        List<ProductResponseDTO> dtoList = List.of(dto1, dto2);

                        Page<ProductResponseDTO> fakeResultPage = new PageImpl<>(dtoList, pageable, dtoList.size());

                        when(productService.getAllProductsPaginated(any(ProductFilterDTO.class), any(Pageable.class)))
                                        .thenReturn(fakeResultPage);

                        mockMvc.perform(
                                        get("/products")
                                                        .param("page", "0")
                                                        .param("size", "2")
                                                        .accept(MediaType.APPLICATION_JSON))
                                        .andExpect(
                                                        status().isOk())
                                        .andExpect(
                                                        content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(
                                                        jsonPath("$.totalElements").value(2))
                                        .andExpect(
                                                        jsonPath("$.totalPages").value(1))
                                        .andExpect(
                                                        jsonPath("$.size").value(2))
                                        .andExpect(
                                                        jsonPath("$.number").value(0))
                                        .andExpect(
                                                        jsonPath("$.content[0].id").value(1L))
                                        .andExpect(
                                                        jsonPath("$.content[0].name").value("Produto A"))
                                        .andExpect(
                                                        jsonPath("$.content[1].id").value(2L))
                                        .andExpect(
                                                        jsonPath("$.content[1].name").value("Produto B"));

                        verify(productService, times(1)).getAllProductsPaginated(any(ProductFilterDTO.class),
                                        any(Pageable.class));
                }

                @Test
                @DisplayName("Deve retornar 200 OK com a página vazia quando não houver dados")
                public void testGetAllProductsPaginated_WhenNoData_shouldReturn200OkAndEmptyPageBody()
                                throws Exception {

                        ProductFilterDTO filter = new ProductFilterDTO();
                        Pageable pageable = PageRequest.of(0, 10);

                        Page<ProductResponseDTO> emptyPage = Page.empty(pageable);

                        when(productService.getAllProductsPaginated(any(ProductFilterDTO.class), any(Pageable.class)))
                                        .thenReturn(emptyPage);

                        mockMvc.perform(
                                        get("/products")
                                                        .param("page", "0")
                                                        .param("size", "10")
                                                        .accept(MediaType.APPLICATION_JSON))
                                        .andExpect(
                                                        status().isOk())
                                        .andExpect(
                                                        content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(
                                                        jsonPath("$.totalElements").value(0))
                                        .andExpect(
                                                        jsonPath("$.totalPages").value(0))
                                        .andExpect(
                                                        jsonPath("$.number").value(0))
                                        .andExpect(
                                                        jsonPath("$.content").isEmpty());

                        verify(productService, times(1)).getAllProductsPaginated(any(ProductFilterDTO.class),
                                        any(Pageable.class));
                }

                @Test
                @DisplayName("Deve retornar 400 Bad Request quando o parâmetro de ordenação for inválido")
                public void testGetAllProductsPaginated_WhenInvalidSortParam_shouldReturn400BadRequest()
                                throws Exception {

                        ProductFilterDTO filter = new ProductFilterDTO();

                        String expectedErrorMessage = "No property 'campoInvalido' found for type 'Product'";

                        when(productService.getAllProductsPaginated(any(ProductFilterDTO.class), any(Pageable.class)))
                                        .thenThrow(new PropertyReferenceException("campoInvalido",
                                                        TypeInformation.of(Product.class), Collections.emptyList()));

                        mockMvc.perform(
                                        get("/products")
                                                        .param("sort", "campoInvalido")
                                                        .accept(MediaType.APPLICATION_JSON))
                                        .andExpect(
                                                        status().isBadRequest())
                                        .andExpect(
                                                        jsonPath("$.status").value(400))
                                        .andExpect(
                                                        jsonPath("$.error").value("Parâmetro de Requisição Inválido"))
                                        .andExpect(
                                                        jsonPath("$.message").value(expectedErrorMessage));

                        verify(productService, times(1)).getAllProductsPaginated(any(ProductFilterDTO.class),
                                        any(Pageable.class));
                }
        }
}
