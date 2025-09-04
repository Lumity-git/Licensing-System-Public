package com.deadzone.license.service;

import com.deadzone.license.config.JwtUtil;
import com.deadzone.license.entity.AdminUser;
import com.deadzone.license.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return adminUserRepository.findByUsernameAndEnabledTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public AdminUser findByUsername(String username) {
        return adminUserRepository.findByUsername(username).orElse(null);
    }

    public void updateLastLogin(String username) {
        adminUserRepository.findByUsername(username).ifPresent(user -> {
            user.updateLastLogin();
            adminUserRepository.save(user);
        });
    }

    public String authenticate(String username, String password) {
        try {
            // Authenticate the user
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );

            // Load user details
            UserDetails userDetails = loadUserByUsername(username);
            
            // Update last login
            updateLastLogin(username);

            // Generate and return JWT token
            return jwtUtil.generateToken(userDetails);

        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid credentials");
        }
    }

    public boolean validateToken(String token) {
        try {
            String username = jwtUtil.extractUsername(token);
            UserDetails userDetails = loadUserByUsername(username);
            return jwtUtil.validateToken(token, userDetails);
        } catch (Exception e) {
            return false;
        }
    }

    public String refreshToken(String oldToken) {
        try {
            String username = jwtUtil.extractUsername(oldToken);
            UserDetails userDetails = loadUserByUsername(username);
            
            if (jwtUtil.validateToken(oldToken, userDetails)) {
                return jwtUtil.generateToken(userDetails);
            } else {
                throw new RuntimeException("Invalid token");
            }
        } catch (Exception e) {
            throw new RuntimeException("Token refresh failed");
        }
    }
}
