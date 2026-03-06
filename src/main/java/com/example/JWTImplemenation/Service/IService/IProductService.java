package com.example.JWTImplemenation.Service.IService;

import com.example.JWTImplemenation.DTO.ProductDTO;
import com.example.JWTImplemenation.Entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IProductService {
    ResponseEntity<Page<ProductDTO>> findAll(Pageable pageable);
     ResponseEntity<ProductDTO> findById(Integer id);
    ResponseEntity<ProductDTO> save(ProductDTO productDTO);
    ResponseEntity<ProductDTO> update(Integer id, Product product);
    ResponseEntity<Void> deleteById(Integer id);
    ResponseEntity<Page<ProductDTO>> searchProducts(String name, String category, Integer minPrice, Integer maxPrice, Pageable pageable);
    ResponseEntity<ProductDTO> addImagesToWatch(Integer watchId, List<MultipartFile> imageFiles);

    ResponseEntity<ProductDTO> update(Integer id, ProductDTO productDTO);

    void updateWatchStatus(List<Integer> watchIds, boolean status, boolean isPaid);

}
