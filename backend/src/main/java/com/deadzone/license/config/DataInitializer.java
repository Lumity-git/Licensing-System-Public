package com.deadzone.license.config;

import com.deadzone.license.entity.AdminUser;
import com.deadzone.license.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.bootstrap.username:admin}")
    private String defaultAdminUsername;

    @Value("${admin.bootstrap.password:change-me-password}")
    private String defaultAdminPassword;

    @Value("${admin.bootstrap.email:admin@example.com}")
    private String defaultAdminEmail;

    @Override
    public void run(String... args) throws Exception {
        // Create default admin user if none exists
        if (adminUserRepository.count() == 0) {
            AdminUser admin = new AdminUser();
            admin.setUsername(defaultAdminUsername);
            admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
            admin.setEmail(defaultAdminEmail);
            admin.setRole("ADMIN");
            admin.setEnabled(true);

            adminUserRepository.save(admin);

            System.out.printf("Created default admin user with username '%s'.%n", defaultAdminUsername);
            System.out.println("Update the bootstrap credentials via environment variables before going live.");
        }
    }
}
