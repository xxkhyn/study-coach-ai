package com.studycoachai.config;

import com.studycoachai.entity.User;
import com.studycoachai.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedDemoUser(UserRepository userRepository) {
        return args -> userRepository.findById(1L)
                .or(() -> userRepository.findByEmail("demo@example.com"))
                .orElseGet(() -> userRepository.save(new User("demo@example.com", "demo", "Demo User")));
    }
}
