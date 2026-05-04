package com.financeProject.MyProject.dto;


// Used when returning user data (GET response)
// NOTE: Password is NOT included (security)

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class UserResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String role;     // role name (not role_id)
    private String status;   // ACTIVE / INACTIVE
}

