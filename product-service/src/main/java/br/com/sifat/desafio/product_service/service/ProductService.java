package br.com.sifat.desafio.product_service.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sifat.desafio.product_service.dto.ProductFilterDTO;
import br.com.sifat.desafio.product_service.dto.ProductRequestDTO;
import br.com.sifat.desafio.product_service.dto.ProductResponseDTO;
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

    ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductMapper mapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado."));
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada."));
    }

    @Transactional()
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {
        Category category = findCategoryById(requestDTO.getCategoryId());
        Product product = mapper.toEntity(requestDTO, category);
        Product savedProduct = productRepository.save(product);
        // TODO: Publicar evento Kafka (CREATE) aqui
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
        // TODO: Publicar evento Kafka (UPDATE) aqui
        return mapper.toResponseDTO(updatedProduct);
    }

    @Transactional()
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Produto não encontrado.");
        }
        productRepository.deleteById(id);
        // TODO: Publicar evento Kafka (DELETE) aqui
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
