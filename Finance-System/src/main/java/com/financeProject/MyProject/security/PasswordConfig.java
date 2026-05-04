package com.financeProject.MyProject.security;

import org.springframework.context.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/*
  Configuration class for password encoding in the application.

  Purpose:
  - Provides centralized password encoding configuration
  - Ensures consistent password hashing across the application
  - Integrates with Spring Security's authentication framework

  BCryptPasswordEncoder Features:
  - Implements strong one-way hashing algorithm
  - Automatically generates random salt for each password
  - Built-in protection against rainbow table attacks
  - Configurable work factor for computational cost

  Security Benefits:
 - Same plaintext password produces different hashes each time
 - Hash includes salt, making pre-computed attacks ineffective
 - Slow algorithm by design to resist brute force attacks
 - Industry standard for password storage in Spring applications

  Usage in Application:
  - Called during user creation to hash new passwords
  - Called during login to compare plaintext with stored hash
  - Automatically used by Spring Security AuthenticationManager
  - Never store or log plaintext passwords

  Default Work Factor:
  - BCrypt default strength is 10 (2^10 iterations)
  - Higher values increase security but reduce performance
  - Can be customized: new BCryptPasswordEncoder(12)

  Integration Points:
  - AdminService uses this encoder when creating users
  - AuthenticationProvider uses this during login validation
  - Password update operations use this for re-hashing
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
