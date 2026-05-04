package com.financeProject.MyProject.repository;

import com.financeProject.MyProject.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
      Repository interface for BlacklistedToken entity operations.
      Provides database access methods for token blacklisting functionality.
 */
@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    /*
      Checks if a token exists in the blacklist.
      @param token The JWT token to check
      @return true if token is blacklisted, false otherwise
    */

    boolean existsByToken(String token);
}
