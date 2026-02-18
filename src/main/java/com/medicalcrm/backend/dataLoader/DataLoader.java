package com.medicalcrm.backend.dataLoader;

import com.medicalcrm.backend.model.Role;
import com.medicalcrm.backend.model.User;
import com.medicalcrm.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.countByRole(Role.ADMIN) == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(new BCryptPasswordEncoder().encode("adminpassword"));
            admin.setRole(Role.ADMIN);
            admin.setEmail("admin@example.com");
            admin.setEnabled(true);
            userRepository.save(admin);

            log.info("Admin user created successfully!");
        } else {
            log.info("Admin user already exists.");
        }
    }
}

