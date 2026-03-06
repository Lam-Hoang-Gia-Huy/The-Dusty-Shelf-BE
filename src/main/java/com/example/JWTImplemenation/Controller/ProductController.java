package com.example.JWTImplemenation.Controller;

import com.example.JWTImplemenation.DTO.ProductDTO;
import com.example.JWTImplemenation.Entities.Product;
import com.example.JWTImplemenation.Service.IService.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/v1/product")
@RequiredArgsConstructor
public class ProductController {

    private final IProductService iProductService;

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(@PageableDefault(size = 12) Pageable pageable) {
        return iProductService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Integer id) {
        return iProductService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        return iProductService.save(productDTO);
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        return iProductService.deleteById(id);
    }

    @PostMapping("/{productId}/images")
    public ResponseEntity<ProductDTO> addImagesToProduct(@PathVariable Integer productId, @RequestParam("imageFiles") List<MultipartFile> imageFiles) {
        return iProductService.addImagesToWatch(productId, imageFiles);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @PageableDefault(size = 12) Pageable pageable) {
        return iProductService.searchProducts(name, category, minPrice, maxPrice, pageable);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Integer id, @RequestBody ProductDTO productDTO) {
        return iProductService.update(id, productDTO);
    }

}
