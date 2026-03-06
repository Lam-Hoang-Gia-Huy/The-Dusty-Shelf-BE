package com.example.JWTImplemenation.Controller;

import com.example.JWTImplemenation.Response.AuthenticationResponse;
import com.example.JWTImplemenation.DTO.AuthenticationRequest;
import com.example.JWTImplemenation.DTO.RefreshTokenRequest;
import com.example.JWTImplemenation.DTO.RegisterRequest;
import com.example.JWTImplemenation.Service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import org.springframework.security.access.prepost.PreAuthorize;


@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService service;
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ){
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/register-staff")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AuthenticationResponse> registerStaff(
            @RequestBody RegisterRequest request
    ){
        return ResponseEntity.ok(service.registerStaff(request));
    }
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ){
        return ResponseEntity.ok(service.authenticate(request));
    }
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @RequestBody RefreshTokenRequest refreshTokenRequest
    ){
        return ResponseEntity.ok(service.refreshToken(refreshTokenRequest));
    }
}
