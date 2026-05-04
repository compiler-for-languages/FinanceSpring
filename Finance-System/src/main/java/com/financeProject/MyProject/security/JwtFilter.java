package com.financeProject.MyProject.security;

import com.financeProject.MyProject.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/*
  JWT authentication filter that intercepts every incoming HTTP request.

  This filter is responsible for extracting, validating, and authenticating
  JWT tokens from the Authorization header of incoming requests.

  Key Responsibilities:
  - Extracts JWT token from the Bearer Authorization header
  - Skips token validation for public endpoints like Swagger UI
  - Checks if the token has been blacklisted during logout
  - Validates token signature and expiration using JwtUtil
  - Loads user details from database using CustomUserDetailsService
  - Sets authentication context in Spring Security for the request
  - Forwards the request to the next filter in the chain upon success

  Filter Execution Flow:
  - The filter runs on every request before reaching the controller
  - Public endpoints are bypassed to allow unauthenticated access
  - Blacklisted tokens are rejected immediately with 403 FORBIDDEN status
  - Valid tokens result in authentication being set in SecurityContextHolder
  - Missing or invalid tokens result in no authentication (401 handled elsewhere)

  Public Endpoints Bypassed:
  - Swagger UI endpoints (/swagger-ui/**)
  - OpenAPI documentation (/v3/api-docs/**)
  - Authentication endpoints like /auth/login are typically also bypassed

  Security Features:
  - Token blacklist validation prevents logged-out tokens from being reused
  - Token extraction only accepts "Bearer " scheme for security standards
  - Authentication is only set if none exists in current context
  - User details are reloaded from database for each authenticated request

  Token Validation Process:
  - Extract token from Authorization header after "Bearer " prefix
  - Extract email (username) from token using JwtUtil.extractEmail()
  - Verify token is not in the blacklist database
  - Validate token signature and expiration using JwtUtil.validateToken()
  - Load complete user details including roles from database
  - Create authentication token with user details and authorities
  - Set authentication in SecurityContextHolder for this request thread

  Error Handling:
  - Blacklisted tokens return HTTP 403 with "Token is logged out" message
  - Invalid or expired tokens result in no authentication context
  - Missing tokens proceed without authentication (handled by controller security)

  Performance Considerations:
  - Database lookup for blacklist check on every request
  - Database lookup for user details on every authenticated request
  - Consider caching for high-traffic applications
  - Swagger endpoints are bypassed to improve documentation performance

  Dependencies:
  - JwtUtil: Handles token generation, extraction, and validation
  - CustomUserDetailsService: Loads user details from database
  - AuthService: Provides token blacklist checking functionality

 */

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        String token = null;
        String username = null;
        String path = request.getServletPath();

        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 🔹 Extract token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtUtil.extractEmail(token); // ✅ use email
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                // Token expired, ignore and proceed unauthenticated
                username = null;
            } catch (Exception e) {
                username = null;
            }
        }

        // 🔹 Validate and authenticate
        if (token != null && authService.isBlacklisted(token)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Token is logged out");
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            var userDetails = userDetailsService.loadUserByUsername(username);
            //compare with username/email, NOT userDetails
            if (jwtUtil.validateToken(token, username)) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}