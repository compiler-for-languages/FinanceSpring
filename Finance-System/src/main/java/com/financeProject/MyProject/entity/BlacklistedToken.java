package com.financeProject.MyProject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
  Entity representing a blacklisted JWT token.
  This entity stores tokens that have been invalidated through logout or security events. Blacklisted tokens are rejected by the authentication filter even if they haven't expired.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BlacklistedToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String token;
}
