package com.example.JWTImplemenation.Service;

import com.example.JWTImplemenation.DTO.UserDTO;
import com.example.JWTImplemenation.Response.AuthenticationResponse;
import com.example.JWTImplemenation.DTO.AuthenticationRequest;
import com.example.JWTImplemenation.DTO.RefreshTokenRequest;
import com.example.JWTImplemenation.DTO.RegisterRequest;
import com.example.JWTImplemenation.Repository.UserRepository;
import com.example.JWTImplemenation.Entities.Role;
import com.example.JWTImplemenation.Entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder PasswordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .name(request.getUserName())
                .email(request.getEmail())
                .createdDate(request.getCreatedDate())
                .avatarUrl(request.getAvatarUrl())
                .status(true)
                .password(PasswordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var userDTO = convertToDTO(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .user(userDTO)
                .build();
    }

    public AuthenticationResponse registerStaff(RegisterRequest request) {
        var user = User.builder()
                .name(request.getUserName())
                .email(request.getEmail())
                .createdDate(request.getCreatedDate())
                .avatarUrl(request.getAvatarUrl())
                .status(true)
                .password(PasswordEncoder.encode(request.getPassword()))
                .role(Role.STAFF)
                .build();
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var userDTO = convertToDTO(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .user(userDTO)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        if (!user.isStatus()) {
            throw new IllegalStateException("User account is not active");
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var token = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        var userDTO = convertToDTO(user);
        return AuthenticationResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(userDTO)
                .build();
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String userEmail = jwtService.extractUsername(refreshTokenRequest.getToken());
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        if (jwtService.isTokenValid(refreshTokenRequest.getToken(), user)) {
            var jwt = jwtService.generateToken(user);
            var userDTO = convertToDTO(user);
            return AuthenticationResponse.builder()
                    .token(jwt)
                    .refreshToken(refreshTokenRequest.getToken())
                    .user(userDTO)
                    .build();
        }
        return null;
    }
    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .password(user.getPassword())
                .name(user.getName())
                .email(user.getEmail())
                .createdDate(user.getCreatedDate())
                .avatarUrl(user.getAvatarUrl())
                .status(user.isStatus())
                .role(user.getRole())
                .build();
    }
}
