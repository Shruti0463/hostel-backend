package com.hostel.controller;

import com.hostel.model.*;
import com.hostel.repo.UserRepository;
import com.hostel.security.JwtUtil;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users; private final PasswordEncoder enc; private final JwtUtil jwt;
    public AuthController(UserRepository u, PasswordEncoder e, JwtUtil j) { users = u; enc = e; jwt = j; }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> b) {
        String email = b.getOrDefault("email", "").trim().toLowerCase();
        String pw = b.getOrDefault("password", "");
        if (b.getOrDefault("name", "").isBlank() || email.isBlank() || pw.length() < 6)
            return ResponseEntity.badRequest().body(Map.of("message", "Name, email and a 6+ character password are required."));
        if (users.existsByEmail(email))
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "This email is already registered."));
        User u = new User();
        u.name = b.get("name").trim(); u.email = email; u.password = enc.encode(pw);
        u.role = Role.STUDENT; u.roomNo = b.get("roomNo");
        users.save(u);
        return ResponseEntity.ok(Map.of("message", "Registered. You can log in now."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> b) {
        var u = users.findByEmail(b.getOrDefault("email", "").trim().toLowerCase()).orElse(null);
        if (u == null || !enc.matches(b.getOrDefault("password", ""), u.password))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Wrong email or password."));
        return ResponseEntity.ok(Map.of("token", jwt.generate(u.email, u.role.name()), "user", u));
    }
}
