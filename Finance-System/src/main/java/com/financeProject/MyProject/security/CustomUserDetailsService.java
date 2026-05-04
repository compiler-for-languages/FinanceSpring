package com.financeProject.MyProject.security;

import com.financeProject.MyProject.repository.UserRepository;
import com.financeProject.MyProject.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/*
  Custom implementation of Spring Security's UserDetailsService for authentication.

  Purpose:
  - Integrates the application's User entity with Spring Security's authentication framework
  - Loads user details by email during the login process
  - Converts database User objects into Spring Security UserDetails objects

  Core Method - loadUserByUsername:
  - Receives user's email as input parameter
  - Queries database to find the corresponding user record
  - Creates Spring Security User object with email, encoded password, and role authorities
  - Throws UsernameNotFoundException if user is not found in database

  Role to Authority Mapping:
  - Role name from database becomes Spring Security authority
  - "VIEWER" in database becomes "ROLE_VIEWER" authority
  - "ANALYST" in database becomes "ROLE_ANALYST" authority
  - "ADMIN" in database becomes "ROLE_ADMIN" authority
  - Can be checked using @PreAuthorize("hasRole('VIEWER')") in controllers

  Integration with Spring Security:
  - Automatically used by AuthenticationManager during login process
  - PasswordEncoder validates provided password against encoded database password
  - Authentication result used for JWT token generation

  Security Considerations:
  - Email field serves as username for authentication and must be unique
  - Password must be encoded using BCrypt or strong hashing algorithm before storage
  - User status validation (ACTIVE vs INACTIVE) should be performed separately
  - Status check is typically implemented in a custom authentication filter

  Future Enhancement Possibilities:
  - Implement isEnabled() method to check account status
  - Implement isAccountNonLocked() for account lockout policies
  - Implement isCredentialsNonExpired() for password expiration handling
  - Implement isAccountNonExpired() for account validity period
 */

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean enabled = "ACTIVE".equalsIgnoreCase(user.getStatus());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                enabled, true, true, true,
                List.of(new SimpleGrantedAuthority(user.getRole().getName()))
        );
    }
}