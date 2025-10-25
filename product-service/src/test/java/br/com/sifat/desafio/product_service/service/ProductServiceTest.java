package br.com.sifat.desafio.product_service.service;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import br.com.sifat.desafio.product_service.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import br.com.sifat.desafio.product_service.repository.CategoryRepository;
import br.com.sifat.desafio.product_service.dto.ProductFilterDTO;
import br.com.sifat.desafio.product_service.dto.ProductRequestDTO;
import br.com.sifat.desafio.product_service.dto.ProductResponseDTO;
import br.com.sifat.desafio.product_service.event.ProductEventDTO;
import br.com.sifat.desafio.product_service.mapper.ProductMapper;
import br.com.sifat.desafio.product_service.model.Category;
import br.com.sifat.desafio.product_service.model.Product;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private KafkaTemplate<String, ProductEventDTO> kafkaTemplate;

    @InjectMocks
    private ProductService productService;

    @Nested
    @DisplayName("getAllCategories Tests")
    class CreateProductTests {
        @Test
        @DisplayName("Retornar ProductDTO criado")
        public void testCreate_WhenSuccessful_shouldReturnCreatedProductDTO() {

            ProductRequestDTO requestDto = new ProductRequestDTO();
            requestDto.setName("Teclado");
            requestDto.setPrice(new BigDecimal("150.00"));
            requestDto.setCategoryId(1L);

            Category category = new Category();
            category.setId(1L);
            category.setName("Periféricos");

            Product product = new Product();
            product.setId(1L);
            product.setName("Teclado");
            product.setPrice(15000L);

            ProductResponseDTO responseDto = new ProductResponseDTO();
            responseDto.setId(1L);
            responseDto.setName("Teclado");
            responseDto.setPrice(new BigDecimal("150.00"));

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

            when(productMapper.toEntity(requestDto, category)).thenReturn(product);

            when(productRepository.save(product)).thenReturn(product);

            when(productMapper.toResponseDTO(product)).thenReturn(responseDto);

            ProductResponseDTO result = productService.createProduct(requestDto);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Teclado", result.getName());

            verify(categoryRepository, times(1)).findById(1L);

            verify(productMapper, times(1)).toEntity(requestDto, category);

            verify(productRepository, times(1)).save(product);

            verify(kafkaTemplate, times(1)).send(
                    anyString(),
                    anyString(),
                    any(ProductEventDTO.class));

            verify(productMapper, times(1)).toResponseDTO(product);
        }

        @Test
        @DisplayName("Lançar EntityNotFoundException quando a categoria não for encontrada")
        public void testCreateProduct_WhenCategoryNotFound_shouldThrowEntityNotFoundException() {

            ProductRequestDTO requestDto = new ProductRequestDTO();
            requestDto.setCategoryId(99L);
            requestDto.setName("Produto com categoria que nãoo existe");

            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
                productService.createProduct(requestDto);
            });

            assertTrue(exception.getMessage().contains("Categoria não encontrada."));

            verify(productMapper, never()).toEntity(any(), any());
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @MockitoSettings(strictness = Strictness.LENIENT)
        @DisplayName("Lançar RuntimeException quando o Kafka falhar persistentemente e não cometer a transação")
        public void testCreateProduct_WhenKafkaFailsPersistently_shouldThrowRuntimeExceptionAndNotCommit() {

            Long existentCategoryId = 1L;
            ProductRequestDTO requestDto = new ProductRequestDTO();
            requestDto.setCategoryId(existentCategoryId);
            requestDto.setName("Produto Teste");

            Category fakeCategory = new Category();
            Product productEntity = new Product();

            when(categoryRepository.findById(existentCategoryId)).thenReturn(Optional.of(fakeCategory));
            when(productMapper.toEntity(any(ProductRequestDTO.class), any(Category.class))).thenReturn(productEntity);
            when(productRepository.save(any(Product.class))).thenReturn(productEntity);

            when(kafkaTemplate.send(anyString(), anyString(), any(ProductEventDTO.class)))
                    .thenThrow(new RuntimeException("Simulando falha fatal no Kafka"));

            assertThrows(RuntimeException.class, () -> {
                productService.createProduct(requestDto);
            });

            verify(productRepository, times(1)).save(any(Product.class));

            verify(categoryRepository, times(1)).findById(existentCategoryId);
            verify(productMapper, times(1)).toEntity(any(ProductRequestDTO.class), any(Category.class));
        }
    }

    @Nested
    @DisplayName("updateProduct Tests")
    class UpdatedProductTests {
        @Test
        @DisplayName("Retornar ProductDTO atualizado")
        public void testUpdateProduct_WhenSuccessful_shouldReturnUpdatedProductDTO() {
            Long productId = 1L;

            ProductRequestDTO requestDto = new ProductRequestDTO();
            requestDto.setName("Mouse");
            requestDto.setPrice(new BigDecimal("80.00"));
            requestDto.setCategoryId(2L);

            Category category = new Category();
            category.setId(2L);
            category.setName("Acessórios");

            Product existingProduct = new Product();
            existingProduct.setId(productId);
            existingProduct.setName("Teclado");
            existingProduct.setPrice(15000L);

            Product updatedProduct = new Product();
            updatedProduct.setId(productId);
            updatedProduct.setName("Mouse");
            updatedProduct.setPrice(8000L);

            ProductResponseDTO responseDto = new ProductResponseDTO();
            responseDto.setId(productId);
            responseDto.setName("Mouse");
            responseDto.setPrice(new BigDecimal("80.00"));

            when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

            when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));

            when(productRepository.save(existingProduct)).thenReturn(updatedProduct);

            when(productMapper.toResponseDTO(updatedProduct)).thenReturn(responseDto);

            ProductResponseDTO result = productService.updateProduct(productId, requestDto);

            assertNotNull(result);
            assertEquals(productId, result.getId());
            assertEquals("Mouse", result.getName());

            verify(productRepository, times(1)).findById(productId);
            verify(categoryRepository, times(1)).findById(2L);
            verify(kafkaTemplate, times(1)).send(
                    anyString(),
                    anyString(),
                    any(ProductEventDTO.class));
            verify(productRepository, times(1)).save(existingProduct);
            verify(productMapper, times(1)).toResponseDTO(updatedProduct);
        }

        @Test
        @DisplayName("Lançar EntityNotFoundException quando o produto não for encontrado")
        public void testUpdateProduct_WhenProductNotFound_shouldThrowEntityNotFoundException() {
            Long productId = 99L;

            ProductRequestDTO requestDto = new ProductRequestDTO();
            requestDto.setName("Produto Inexistente");
            requestDto.setPrice(new BigDecimal("100.00"));
            requestDto.setCategoryId(1L);

            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
                productService.updateProduct(productId, requestDto);
            });

            assertTrue(exception.getMessage().contains("Produto não encontrado."));

            verify(categoryRepository, never()).findById(any());
            verify(productRepository, never()).save(any(Product.class));
            verify(productMapper, never()).toResponseDTO(any());
        }

        @Test
        @DisplayName("Lançar EntityNotFoundException quando a categoria não for encontrada")
        public void testUpdateProduct_WhenCategoryNotFound_shouldThrowEntityNotFoundException() {
            Long productId = 1L;

            ProductRequestDTO requestDto = new ProductRequestDTO();
            requestDto.setName("Produto com categoria inválida");
            requestDto.setPrice(new BigDecimal("120.00"));
            requestDto.setCategoryId(99L);

            Product existingProduct = new Product();
            existingProduct.setId(productId);
            existingProduct.setName("Teclado");
            existingProduct.setPrice(15000L);

            when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
                productService.updateProduct(productId, requestDto);
            });

            assertTrue(exception.getMessage().contains("Categoria não encontrada."));

            verify(productRepository, times(1)).findById(productId);
            verify(productRepository, never()).save(any(Product.class));
            verify(productMapper, never()).toResponseDTO(any());
        }
    }

    @Nested
    @DisplayName("deleteProduct Tests")
    class DeleteProductTests {
        @Test
        @DisplayName("Deve invocar o repositório para deletar o produto")
        public void testDeleteProduct_WhenSuccessful_shouldInvokeRepositoryDelete() {
            Long productId = 1L;

            Product existingProduct = new Product();
            existingProduct.setId(productId);
            existingProduct.setName("Teclado");
            existingProduct.setPrice(15000L);

            when(productRepository.existsById(productId)).thenReturn(true);

            productService.deleteProduct(productId);

            verify(kafkaTemplate, times(1)).send(
                    anyString(),
                    anyString(),
                    any(ProductEventDTO.class));
            verify(productRepository, times(1)).existsById(productId);
            verify(productRepository, times(1)).deleteById(productId);
        }

        @Test
        @DisplayName("Lançar EntityNotFoundException quando o produto não for encontrado")
        public void testDeleteProduct_WhenProductNotFound_shouldThrowEntityNotFoundException() {
            Long productId = 99L;

            when(productRepository.existsById(productId)).thenReturn(false);

            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
                productService.deleteProduct(productId);
            });

            assertTrue(exception.getMessage().contains("Produto não encontrado."));

            verify(productRepository, times(1)).existsById(productId);
            verify(productRepository, never()).deleteById(productId);
        }
    }

    @Nested
    @DisplayName("getProductById Tests")
    class GetProductByIdTests {
        @Test
        @DisplayName("Deve retornar ProductDTO quando o produto for encontrado")
        public void testGetProductById_WhenSuccessful_shouldReturnProductDTO() {
            Long productId = 1L;

            Product product = new Product();
            product.setId(productId);
            product.setName("Teclado");
            product.setPrice(15000L);

            ProductResponseDTO responseDto = new ProductResponseDTO();
            responseDto.setId(productId);
            responseDto.setName("Teclado");
            responseDto.setPrice(new BigDecimal("150.00"));

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            when(productMapper.toResponseDTO(product)).thenReturn(responseDto);

            ProductResponseDTO result = productService.getProductById(productId);

            assertNotNull(result);
            assertEquals(productId, result.getId());
            assertEquals("Teclado", result.getName());

            verify(productRepository, times(1)).findById(productId);
            verify(productMapper, times(1)).toResponseDTO(product);
        }

        @Test
        @DisplayName("Lançar EntityNotFoundException quando o produto não for encontrado")
        public void testGetProductById_WhenProductNotFound_shouldThrowEntityNotFoundException() {
            Long productId = 99L;

            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
                productService.getProductById(productId);
            });

            assertTrue(exception.getMessage().contains("Produto não encontrado."));

            verify(productRepository, times(1)).findById(productId);
            verify(productMapper, never()).toResponseDTO(any());
        }
    }

    @Nested
    @DisplayName("getAllProductsPaginated Tests")
    class GetAllProductsPaginatedTests {
        @Test
        @DisplayName("Deve retornar uma paginacao de ProductDTOs")
        public void testGetAllProductsPaginated_WhenSuccessful_shouldReturnPageOfProductDTOs() {
            ProductFilterDTO filter = new ProductFilterDTO();
            Pageable pageable = PageRequest.of(0, 10);

            Product product1 = new Product();
            product1.setId(1L);
            product1.setName("Teclado");
            product1.setPrice(15000L);

            Product product2 = new Product();
            product2.setId(2L);
            product2.setName("Mouse");
            product2.setPrice(8000L);

            List<Product> productList = List.of(product1, product2);

            Page<Product> fakeProductPage = new PageImpl<>(productList, pageable, productList.size());

            ProductResponseDTO responseDto1 = new ProductResponseDTO();
            responseDto1.setId(1L);
            responseDto1.setName("Teclado");
            responseDto1.setPrice(new BigDecimal("150.00"));

            ProductResponseDTO responseDto2 = new ProductResponseDTO();
            responseDto2.setId(2L);
            responseDto2.setName("Mouse");
            responseDto2.setPrice(new BigDecimal("80.00"));

            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(fakeProductPage);

            when(productMapper.toResponseDTO(product1)).thenReturn(responseDto1);
            when(productMapper.toResponseDTO(product2)).thenReturn(responseDto2);

            Page<ProductResponseDTO> resultPage = productService.getAllProductsPaginated(filter, pageable);

            assertNotNull(resultPage);
            assertEquals(2, resultPage.getTotalElements());
            assertEquals(2, resultPage.getContent().size());
            assertEquals("Teclado", resultPage.getContent().get(0).getName());

            verify(productRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));

            verify(productMapper, times(1)).toResponseDTO(product1);
            verify(productMapper, times(1)).toResponseDTO(product2);

            verify(productRepository, never()).findAll();
        }

        @Test
        @DisplayName("Deve retornar uma paginação vazia quando nenhum produto for encontrado")
        public void testGetAllProductsPaginated_WhenNoProductsFound_shouldReturnEmptyPage() {
            ProductFilterDTO filter = new ProductFilterDTO();
            Pageable pageable = PageRequest.of(0, 10);

            List<Product> emptyProductList = List.of();

            Page<Product> emptyProductPage = new PageImpl<>(emptyProductList, pageable, 0);

            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(emptyProductPage);

            Page<ProductResponseDTO> resultPage = productService.getAllProductsPaginated(filter, pageable);

            assertNotNull(resultPage);
            assertEquals(0, resultPage.getTotalElements());
            assertEquals(0, resultPage.getContent().size());

            verify(productRepository, times(1))
                    .findAll(any(Specification.class), any(Pageable.class));

            verify(productMapper, never()).toResponseDTO(any());
        }
    }
}