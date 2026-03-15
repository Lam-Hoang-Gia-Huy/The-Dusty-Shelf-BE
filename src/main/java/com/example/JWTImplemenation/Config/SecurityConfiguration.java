package com.example.JWTImplemenation.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
        private final JwtAuthenticationFilter JwtAuthFilter;
        private final AuthenticationProvider authenticationProvider;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(Customizer.withDefaults())
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/v1/auth/**").permitAll()
                                                .requestMatchers("/error").permitAll()
                                                .requestMatchers("/ws/**").permitAll()
                                                .requestMatchers("/api/ai/chat").permitAll()
                                                .requestMatchers("/api/v1/product", "/api/v1/product/{id}",
                                                                "/api/v1/product/user/{id}", "/api/v1/product/search")
                                                .permitAll()
                                                .requestMatchers("/api/v1/category", "/api/v1/category/**").permitAll()
                                                .requestMatchers("/api/v1/user", "/api/v1/user/{id}").permitAll()
                                                .requestMatchers("/api/v1/appraisal", "/api/v1/appraisal/{id}")
                                                .permitAll()
                                                .requestMatchers("/api/v1/feedback/product/{productId}").permitAll()
                                                .anyRequest().authenticated())
                                .authenticationProvider(authenticationProvider)
                                .addFilterBefore(JwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }
}
