package com.financeProject.MyProject.config;

import com.financeProject.MyProject.model.Role;
import com.financeProject.MyProject.model.User;
import com.financeProject.MyProject.repository.RoleRepository;
import com.financeProject.MyProject.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        
        // Initialize Roles if they don't exist
        if (roleRepository.count() == 0) {
            roleRepository.saveAll(List.of(
                    new Role(null, "VIEWER"),
                    new Role(null, "ANALYST"),
                    new Role(null, "ADMIN")
            ));
        }

        if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Role not found"));

            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(adminRole);
            admin.setStatus("ACTIVE");

            userRepository.save(admin);
        }
    }
}
