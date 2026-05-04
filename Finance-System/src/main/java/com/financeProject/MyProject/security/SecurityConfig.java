package com.financeProject.MyProject.security;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import java.util.Arrays;

/*
  Core security configuration class for the application.

  Purpose:
  - Defines security rules and access control for all HTTP endpoints
  - Configures authentication and authorization mechanisms
  - Integrates JWT filter into Spring Security filter chain

  Security Configuration Details:
  - CSRF protection is disabled because JWT tokens are stateless
  - Session management is stateless (no server-side session storage)
  - JWT filter is added before UsernamePasswordAuthenticationFilter
  - AuthenticationManager is exposed for login endpoint usage
  - CORS is enabled to allow cross-origin requests from the frontend

  Public Endpoints (No Authentication Required):
  - /auth/** : Login, logout, and authentication endpoints
  - /swagger-ui/** : Swagger UI documentation interface
  - /swagger-ui.html : Swagger UI main page
  - /v3/api-docs/** : OpenAPI documentation endpoints

  Role-Based Access Rules:
  - /admin/** : Restricted to users with ADMIN authority only
  - /users/** : Requires authentication (any logged-in user)
  - All other endpoints : Require authentication

  Filter Chain Order:
  - JwtFilter executes first to validate JWT token
  - UsernamePasswordAuthenticationFilter executes after JWT filter
  - Request proceeds to controller if authentication passes

  AuthenticationManager Bean:
  - Exposed as Spring bean for dependency injection
  - Used by AuthController during login process
  - Integrates with CustomUserDetailsService and PasswordEncoder
  - Obtained from AuthenticationConfiguration

  Security Flow:
  - Request arrives → JwtFilter validates token → Sets authentication context
  - Request reaches SecurityConfig → Role/URL matching occurs
  - Access granted or denied based on authentication and authorities
  - 401 Unauthorized if no authentication present
  - 403 Forbidden if authenticated but insufficient privileges
 */
@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**","/swagger-ui/**","/swagger-ui.html","/v3/api-docs/**").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/users/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); // Allow any origin for development
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}