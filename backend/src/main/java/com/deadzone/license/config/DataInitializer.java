package com.deadzone.license.config;

import com.deadzone.license.entity.AdminUser;
import com.deadzone.license.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create default admin user if none exists
        if (adminUserRepository.count() == 0) {
            AdminUser admin = new AdminUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("si83j2992JCnx"));
            admin.setEmail("admin@deadzone.com");
            admin.setRole("ADMIN");
            admin.setEnabled(true);
            
            adminUserRepository.save(admin);
            
            System.out.println("Created default admin user:");
            System.out.println("Username: admin");
            System.out.println("Password: si83j2992JCnx");
            System.out.println("Please change the default password after first login!");
        }
    }
}
