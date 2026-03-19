package com.example.JWTImplemenation;

import com.example.JWTImplemenation.DTO.ProductDTO;
import com.example.JWTImplemenation.Service.IService.IProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
class ReproduceErrorTest {

    @Autowired
    private IProductService productService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetProductByIdService() {
        try {
            ResponseEntity<ProductDTO> response = productService.findById(4);
            System.out.println("Service Response Status: " + response.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testGetProductByIdController() throws Exception {
        mockMvc.perform(get("/api/v1/product/4"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
