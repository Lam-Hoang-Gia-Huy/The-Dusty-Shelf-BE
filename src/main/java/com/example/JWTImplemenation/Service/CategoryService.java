package com.example.JWTImplemenation.Service;

import com.example.JWTImplemenation.DTO.CategoryDTO;
import com.example.JWTImplemenation.Entities.Category;
import com.example.JWTImplemenation.Repository.CategoryRepository;
import com.example.JWTImplemenation.Service.IService.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public ResponseEntity<List<CategoryDTO>> findAll() {
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<CategoryDTO> findById(Integer id) {
        return categoryRepository.findById(id)
                .map(category -> ResponseEntity.ok(convertToDTO(category)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<CategoryDTO> save(CategoryDTO categoryDTO) {
        Category category = Category.builder()
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .build();
        Category savedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(convertToDTO(savedCategory));
    }

    @Override
    public ResponseEntity<CategoryDTO> update(Integer id, CategoryDTO categoryDTO) {
        return categoryRepository.findById(id)
                .map(existingCategory -> {
                    existingCategory.setName(categoryDTO.getName());
                    existingCategory.setDescription(categoryDTO.getDescription());
                    Category updatedCategory = categoryRepository.save(existingCategory);
                    return ResponseEntity.ok(convertToDTO(updatedCategory));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Void> deleteById(Integer id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private CategoryDTO convertToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
