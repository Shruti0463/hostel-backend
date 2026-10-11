package com.hostel.controller;

import com.hostel.model.*;
import com.hostel.repo.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository users; private final ComplaintRepository complaints; private final PasswordEncoder enc;
    public AdminController(UserRepository u, ComplaintRepository c, PasswordEncoder e) { users = u; complaints = c; enc = e; }

    @GetMapping("/students") public List<User> students() { return users.findByRole(Role.STUDENT); }
    @GetMapping("/staff") public List<User> staff() { return users.findByRole(Role.STAFF); }

    @PostMapping("/staff")
    public ResponseEntity<?> addStaff(@RequestBody Map<String, String> b) {
        String email = b.getOrDefault("email", "").trim().toLowerCase();
        if (b.getOrDefault("name", "").isBlank() || email.isBlank() || b.getOrDefault("password", "").length() < 6)
            return ResponseEntity.badRequest().body(Map.of("message", "Name, email and a 6+ character password are required."));
        if (users.existsByEmail(email)) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Email already in use."));
        User u = new User();
        u.name = b.get("name"); u.email = email; u.password = enc.encode(b.get("password"));
        u.role = Role.STAFF; u.specialization = b.get("specialization");
        return ResponseEntity.ok(users.save(u));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        var u = users.findById(id).orElse(null);
        if (u == null || u.role == Role.ADMIN) return ResponseEntity.badRequest().build();
        try { users.delete(u); return ResponseEntity.noContent().build(); }
        catch (Exception e) { return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "This user still has complaints linked to them.")); }
    }

    @GetMapping("/stats")
    public Map<String, Long> stats() {
        Map<String, Long> m = new LinkedHashMap<>();
        m.put("total", complaints.count());
        for (Status s : Status.values()) m.put(s.name(), complaints.countByStatus(s));
        m.put("students", (long) users.findByRole(Role.STUDENT).size());
        m.put("staff", (long) users.findByRole(Role.STAFF).size());
        return m;
    }
}
