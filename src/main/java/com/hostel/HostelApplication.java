package com.hostel;

import com.hostel.model.*;
import com.hostel.repo.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class HostelApplication {
    public static void main(String[] args) { SpringApplication.run(HostelApplication.class, args); }

    /** Creates the default admin on first start. */
    @Bean
    CommandLineRunner seed(UserRepository users, PasswordEncoder enc,
                           @Value("${app.admin.email}") String email,
                           @Value("${app.admin.password}") String pw) {
        return a -> {
            if (!users.existsByEmail(email)) {
                User u = new User();
                u.name = "Administrator"; u.email = email; u.password = enc.encode(pw); u.role = Role.ADMIN;
                users.save(u);
            }
        };
    }
}
