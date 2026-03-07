package com.example.JWTImplemenation.Config;

import com.cloudinary.Cloudinary;
import lombok.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {
    @org.springframework.beans.factory.annotation.Value("${cloudinary.cloud_name}")
    private String cloudName;

    @org.springframework.beans.factory.annotation.Value("${cloudinary.api_key}")
    private String apiKey;

    @org.springframework.beans.factory.annotation.Value("${cloudinary.api_secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        return new Cloudinary(config);
    }
}