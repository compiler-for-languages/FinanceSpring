package com.financeProject.MyProject.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/*
  Utility class for JWT (JSON Web Token) operations.

  Purpose:
  - Handles generation, validation, and extraction of JWT tokens
  - Provides stateless authentication mechanism for the application
  - Eliminates server-side session storage requirements

  Token Generation:
  - Creates JWT token using user email as the subject
  - Sets issued timestamp for token creation time
  - Sets expiration time (default: 1 hour from creation)
  - Signs token with HMAC-SHA256 secret key for tamper protection

  Token Validation Process:
  - Extracts email from the token
  - Compares extracted email with provided email
  - Checks if token has expired by comparing expiration date with current time
  - Validates signature integrity using the secret key
  - Token is valid only if all checks pass

  Security Configuration:
  - Secret key: "mysecretkeymysecretkeymysecretkey" (must be at least 32 bytes for HS256)
  - Expiration: 3600000 milliseconds (1 hour)
  - Algorithm: HMAC-SHA256 (HS256) for symmetric signing

  Token Structure:
  - Header: Algorithm and token type
  - Payload: Subject (email), Issued At (iat), Expiration (exp)
  - Signature: Signed using secret key to verify integrity

  Methods Overview:
  - generateToken(String email): Creates new JWT token for authenticated user
  - extractEmail(String token): Retrieves email subject from token
  - validateToken(String token, String email): Verifies token validity and email match
  - extractAllClaims(String token): Parses and extracts all claims from token
  - isTokenExpired(String token): Checks if token's expiration time has passed
  - getKey(): Converts secret string to cryptographic Key object

  Security Notes:
  - Secret key should be moved to environment variables or configuration file
  - Production key must be at least 32 characters for HS256 algorithm
  - Consider using RSA key pair for distributed systems
  - Tokens cannot be revoked naturally (requires blacklist mechanism)
  - Shorter expiration times improve security but reduce user convenience

  Usage Flow:
  - Login controller calls generateToken() after successful authentication
  - Client receives token and includes in Authorization header
  - JwtFilter calls validateToken() for each authenticated request
  - extractEmail() is used to identify the user from token
 */

@Component
public class JwtUtil {

    private final String SECRET = "mysecretkeymysecretkeymysecretkey";
    private final long EXPIRATION = 1000 * 60 * 60;

    private Key getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }



    public boolean validateToken(String token, String email) {
        return email.equals(extractEmail(token)) && !isTokenExpired(token);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}