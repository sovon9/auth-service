package com.sovon9.authentication_service.config;

import com.sovon9.authentication_service.entities.User;
import com.sovon9.authentication_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DefaultUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultUserSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Override these in application.properties if needed:
    //   app.default-user.username=admin
    //   app.default-user.password=changeme
    //   app.default-user.role=ROLE_ADMIN
    @Value("${app.default-user.username:admin}")
    private String defaultUsername;

    @Value("${app.default-user.password:changeme}")
    private String defaultPassword;

    @Value("${app.default-user.role:ROLE_ADMIN}")
    private String defaultRole;

    public DefaultUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Idempotent: only seed if the default user does not already exist
        if (userRepository.findByUsername(defaultUsername).isPresent()) {
            log.info("Default user '{}' already exists — skipping seed.", defaultUsername);
            return;
        }

        User defaultUser = new User();
        defaultUser.setUsername(defaultUsername);
        defaultUser.setPassword(passwordEncoder.encode(defaultPassword));
        defaultUser.setRole(defaultRole);
        defaultUser.setCreateTime(LocalDateTime.now());

        userRepository.save(defaultUser);
        log.info("Default user '{}' created successfully with role '{}'.", defaultUsername, defaultRole);
    }
}
