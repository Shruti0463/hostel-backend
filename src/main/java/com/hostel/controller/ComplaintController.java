package com.hostel.controller;

import com.hostel.model.*;
import com.hostel.repo.*;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {
    private final ComplaintRepository complaints; private final UserRepository users;
    public ComplaintController(ComplaintRepository c, UserRepository u) { complaints = c; users = u; }

    private User me(Authentication a) { return users.findByEmail(a.getName()).orElseThrow(); }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Complaint in, Authentication a) {
        if (in.title == null || in.title.isBlank() || in.location == null || in.location.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "Title and location are required."));
        Complaint c = new Complaint();
        c.title = in.title; c.description = in.description; c.category = in.category;
        c.location = in.location; c.priority = in.priority == null ? "MEDIUM" : in.priority;
        c.student = me(a);
        return ResponseEntity.ok(complaints.save(c));
    }

    @GetMapping("/my") public List<Complaint> mine(Authentication a) { return complaints.findByStudentOrderByCreatedAtDesc(me(a)); }
    @GetMapping("/assigned") public List<Complaint> assigned(Authentication a) { return complaints.findByAssignedStaffOrderByCreatedAtDesc(me(a)); }
    @GetMapping public List<Complaint> all() { return complaints.findAllByOrderByCreatedAtDesc(); }

    @PutMapping("/{id}/assign/{staffId}")
    public ResponseEntity<?> assign(@PathVariable Long id, @PathVariable Long staffId) {
        var c = complaints.findById(id).orElse(null); var s = users.findById(staffId).orElse(null);
        if (c == null || s == null || s.role != Role.STAFF) return ResponseEntity.badRequest().body(Map.of("message", "Invalid complaint or staff."));
        c.assignedStaff = s;
        if (c.status == Status.PENDING) c.status = Status.ASSIGNED;
        return ResponseEntity.ok(complaints.save(c));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> status(@PathVariable Long id, @RequestBody Map<String, String> b, Authentication a) {
        var c = complaints.findById(id).orElse(null);
        if (c == null) return ResponseEntity.notFound().build();
        User u = me(a);
        if (u.role == Role.STAFF && (c.assignedStaff == null || !c.assignedStaff.id.equals(u.id)))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "This complaint is not assigned to you."));
        c.status = Status.valueOf(b.get("status"));
        c.remarks = b.get("remarks");
        c.resolvedAt = c.status == Status.RESOLVED ? LocalDateTime.now() : null;
        return ResponseEntity.ok(complaints.save(c));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication a) {
        var c = complaints.findById(id).orElse(null);
        if (c == null) return ResponseEntity.notFound().build();
        User u = me(a);
        boolean owner = c.student != null && c.student.id.equals(u.id) && c.status == Status.PENDING;
        if (u.role != Role.ADMIN && !owner) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        complaints.delete(c);
        return ResponseEntity.noContent().build();
    }
}
