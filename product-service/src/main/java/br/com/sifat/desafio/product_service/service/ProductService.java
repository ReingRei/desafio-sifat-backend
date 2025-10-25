package br.com.sifat.desafio.product_service.service;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sifat.desafio.product_service.config.KafkaProducerConfig;
import br.com.sifat.desafio.product_service.dto.ProductFilterDTO;
import br.com.sifat.desafio.product_service.dto.ProductRequestDTO;
import br.com.sifat.desafio.product_service.dto.ProductResponseDTO;
import br.com.sifat.desafio.product_service.event.ProductEventDTO;
import br.com.sifat.desafio.product_service.mapper.ProductMapper;
import br.com.sifat.desafio.product_service.model.Category;
import br.com.sifat.desafio.product_service.model.Product;
import br.com.sifat.desafio.product_service.repository.CategoryRepository;
import br.com.sifat.desafio.product_service.repository.ProductRepository;
import br.com.sifat.desafio.product_service.repository.specification.ProductSpecification;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper mapper;
    private final KafkaTemplate<String, ProductEventDTO> kafkaTemplate;

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 50;

    ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductMapper mapper,
            KafkaTemplate<String, ProductEventDTO> kafkaTemplate) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado."));
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada."));
    }

    private void sendProductEvent(ProductEventDTO.EventType type, Product product, Long productId) {
        ProductEventDTO event = new ProductEventDTO();
        event.setType(type);
        event.setProductId(productId);

        if (product != null) {
            event.setName(product.getName());
            event.setPrice(new BigDecimal(product.getPrice()).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
            if (product.getCategory() != null) {
                event.setCategoryId(product.getCategory().getId());
            }
        }

        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                kafkaTemplate.send(KafkaProducerConfig.TOPIC_NAME, productId.toString(), event);
                return;
            } catch (Exception e) {
                attempts++;
                System.err.println("WARN KAFKA (Tentativa " + attempts + "/" + MAX_RETRIES
                        + ") Falha ao enviar evento para Produto ID " + productId + ": " + e.getMessage());

                if (attempts >= MAX_RETRIES) {
                    throw new RuntimeException("Falha de conexão persistente com Kafka após " + MAX_RETRIES
                            + " tentativas. A transação será revertida.");
                }

                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    @Transactional()
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {
        Category category = findCategoryById(requestDTO.getCategoryId());
        Product product = mapper.toEntity(requestDTO, category);

        Product savedProduct = productRepository.save(product);

        sendProductEvent(ProductEventDTO.EventType.CREATED, savedProduct, savedProduct.getId());

        return mapper.toResponseDTO(savedProduct);
    }

    @Transactional()
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO requestDTO) {
        Product existingProduct = findProductById(id);
        Category category = findCategoryById(requestDTO.getCategoryId());

        existingProduct.setName(requestDTO.getName());
        existingProduct.setImageUrl(requestDTO.getImageUrl());
        existingProduct.setCategory(category);
        existingProduct.setPrice(requestDTO.getPrice().multiply(new java.math.BigDecimal(100)).longValue());

        Product updatedProduct = productRepository.save(existingProduct);

        sendProductEvent(ProductEventDTO.EventType.UPDATED, updatedProduct, updatedProduct.getId());

        return mapper.toResponseDTO(updatedProduct);
    }

    @Transactional()
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Produto não encontrado.");
        }
        productRepository.deleteById(id);

        sendProductEvent(ProductEventDTO.EventType.DELETED, null, id);

        return;
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        Product product = findProductById(id);
        return mapper.toResponseDTO(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getAllProductsPaginated(ProductFilterDTO filter, Pageable pageable) {
        Specification<Product> spec = ProductSpecification.filterBy(
                filter.getName(),
                filter.getCategoryId(),
                filter.getMinPrice(),
                filter.getMaxPrice());

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        return productPage.map(mapper::toResponseDTO);
    }
}
