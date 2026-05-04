package com.financeProject.MyProject.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
// Used when creating a new user (POST /users)
public class UserRequestDTO {

    private String name;       // User name
    private String email;      // Must be unique
    private String password;   // Raw password (later can hash)
    private String role;       // VIEWER / ANALYST / ADMIN
}