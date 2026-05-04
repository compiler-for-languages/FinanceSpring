package com.financeProject.MyProject.controller;
import com.financeProject.MyProject.dto.AuthRequestDTO;
import com.financeProject.MyProject.dto.AuthResponseDTO;
import com.financeProject.MyProject.security.JwtUtil;
import com.financeProject.MyProject.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/*
      REST Controller for handling authentication operations.

      This controller provides endpoints for user authentication including:
      - User login with email/password
      - JWT token generation upon successful authentication
      - User logout with token blacklisting

      All authentication endpoints are publicly accessible (no token required)
      to allow users to login and logout.

      Security Flow:
      1. User submits credentials to /login
      2. System validates credentials and returns JWT token
      3. User includes token in Authorization header for subsequent requests
      4. User calls /logout to invalidate the token

 */
import com.financeProject.MyProject.repository.UserRepository;
import com.financeProject.MyProject.model.User;
import com.financeProject.MyProject.dto.UserResponseDTO;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

    @RestController
    @RequestMapping("/auth") // Base path for authentication endpoints
    public class AuthController {

        @Autowired
        private AuthenticationManager authenticationManager;

        @Autowired
        private JwtUtil jwtUtil;

        @Autowired
        private AuthService authService;

        @Autowired
        private UserRepository userRepository;

    /*
          Authenticates a user and generates a JWT token.

          Endpoint: POST /auth/login

          This method validates user credentials (email and password) and
          returns a signed JWT token for authenticated access to protected
          resources. The token should be included in subsequent requests
          as a Bearer token in the Authorization header.

          Authentication Process:
          1. Receives email and password from request body
          2. Delegates to Spring Security's AuthenticationManager
          3. Validates credentials against database
          4. Generates JWT token with user email as subject
          5. Returns token to client for storage (localStorage/session)

          Token Information:
          - Contains user email as subject
          - Includes roles/authorities for authorization
          - Has expiration time (configured in JwtUtil)
          - Signed with secret key to prevent tampering

          @param request AuthRequestDTO containing:
                         - email: User's email address (used as username)
                         - password: User's plain text password

          @return AuthResponseDTO containing JWT token for authenticated sessions

          @throws AuthenticationException if credentials are invalid:
                  - User not found
                  - Password doesn't match
                  - Account is locked or disabled
         * @see AuthRequestDTO
         * @see AuthResponseDTO
         * @see JwtUtil#generateToken(String)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("message", "User does not exist"));
        }
        if ("INACTIVE".equalsIgnoreCase(user.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("message", "User is inactive / blacklisted"));
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            String token = jwtUtil.generateToken(request.getEmail());
            
            UserResponseDTO userDTO = new UserResponseDTO(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole().getName(),
                    user.getStatus()
            );

            return ResponseEntity.ok(new AuthResponseDTO(token, userDTO));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("message", "Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("message", "Login failed"));
        }
    }

    /*
      Logs out a user by blacklisting their JWT token.

      Endpoint: POST /auth/logout

      This method invalidates the provided JWT token by adding it to
      the blacklist. Once blacklisted, the token cannot be used for
      any further authenticated requests, effectively logging the user out.

      Why Token Blacklisting is Needed:
      - JWT tokens are stateless and cannot be naturally revoked
      - Without blacklisting, tokens remain valid until expiration
      - Blacklisting ensures immediate logout functionality

      Logout Process:
      1. Extract JWT token from Authorization header
      2. Validate token format (Bearer scheme)
      3. Add token to blacklist database
      4. Future requests with this token are rejected

      Header Format:
      Authorization: Bearer <jwt-token-string>

      Security Considerations:
      - Tokens remain technically valid but are rejected via blacklist check
      - Consider implementing token expiration cleanup job
      - Blacklist should be checked in authentication filter

      @param request HttpServletRequest containing Authorization header
                     with Bearer token

      @return String message indicating logout status:
              - "Logged out successfully" when token is blacklisted
              - "No token provided" when Authorization header is missing
              - Invalid format messages (handled in improved version)

      @note This is a simplified implementation. Production version should:
            - Validate token signature before blacklisting
            - Check if token is already blacklisted
            - Extract token expiration for scheduled cleanup
            - Return appropriate HTTP status codes
     */

        @PostMapping("/logout")
        public ResponseEntity<?> logout(HttpServletRequest request) {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                authService.logout(token);
                return ResponseEntity.ok("Logged out successfully");
            }

            return ResponseEntity.badRequest().body("No token provided");
        }
    }



