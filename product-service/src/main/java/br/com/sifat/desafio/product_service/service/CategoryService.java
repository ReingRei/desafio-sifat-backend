package br.com.sifat.desafio.product_service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sifat.desafio.product_service.mapper.ProductMapper;
import br.com.sifat.desafio.product_service.repository.CategoryRepository;
import br.com.sifat.desafio.product_service.dto.CategoryDTO;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductMapper mapper;

    CategoryService(CategoryRepository categoryRepository, ProductMapper mapper) {
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(mapper::toCategoryDTO)
                .collect(Collectors.toList());
    }
}
