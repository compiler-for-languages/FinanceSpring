package com.financeProject.MyProject.service;

import com.financeProject.MyProject.entity.BlacklistedToken;
import com.financeProject.MyProject.repository.BlacklistedTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
      Service layer for handling authentication-related operations.
      This service manages token blacklisting functionality to support secure logout operations and invalidate JWT tokens before their natural expiration.
      Key Features:
      - Blacklists JWT tokens during logout
      - Validates if a token has been blacklisted
      - Prevents reuse of logged-out tokens

      Why Token Blacklisting?
      - JWT tokens are stateless and cannot be revoked naturally
      - Blacklisting provides a way to invalidate tokens on demand
      - Essential for logout functionality and security compliance
 */
@Service
public class AuthService {

    /*
      Logs out a user by blacklisting their JWT token.

      This method adds the provided JWT token to the blacklist,
      making it invalid for future authentication attempts.
      Once blacklisted, the token cannot be used to access any
      protected endpoints.

      Workflow:
      1. User sends logout request with their JWT token
      2. This method saves the token to blacklist table
      3. Any future request with this token is rejected
      4. User must re-authenticate to get a new token

      Important Notes:
      - Original JWT tokens remain technically valid until expiration
      - Blacklist check happens before each authenticated request
      - Consider implementing scheduled cleanup for expired tokens

      @param token -  The JWT token to be blacklisted (must not be null)

      @throws org.springframework.dao.DataAccessException if database operation fails

      @see BlacklistedToken
      @see BlacklistedTokenRepository

      @example Usage:
      String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
      authService.logout(jwtToken);
     //  Token is now blacklisted and cannot be used
     */
    @Autowired
    private BlacklistedTokenRepository blacklistRepo;

    public void logout(String token) {

        BlacklistedToken bt = new BlacklistedToken();
        bt.setToken(token);

        blacklistRepo.save(bt);
    }

    /*
      Checks whether a given JWT token has been blacklisted.

      This method is typically called during authentication middleware
      to validate if a token is still valid. Blacklisted tokens are
      rejected even if they haven't expired yet.

      When to use:
      - Before processing any authenticated request
      - In JWT authentication filter/interceptor
      - During token validation pipeline

      Security Impact:
      - Prevents token reuse after logout
      - Supports token revocation on security incidents
      - Enforces session invalidation

      @param token The JWT token to check (can be null)
      @return true if token exists in blacklist, false otherwise

      @note Performance consideration: This query executes on every request
            Consider caching for high-traffic applications

      @example Usage in Authentication Filter:
      if (authService.isBlacklisted(jwtToken)) {
          throw new RuntimeException("Token has been invalidated");
      }

      @see #logout(String)
     */
    public boolean isBlacklisted(String token) {
        return blacklistRepo.existsByToken(token);
    }
}
