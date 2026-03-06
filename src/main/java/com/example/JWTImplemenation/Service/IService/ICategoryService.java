package com.example.JWTImplemenation.Service.IService;

import com.example.JWTImplemenation.DTO.CategoryDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ICategoryService {
    ResponseEntity<List<CategoryDTO>> findAll();
    ResponseEntity<CategoryDTO> findById(Integer id);
    ResponseEntity<CategoryDTO> save(CategoryDTO categoryDTO);
    ResponseEntity<CategoryDTO> update(Integer id, CategoryDTO categoryDTO);
    ResponseEntity<Void> deleteById(Integer id);
}
