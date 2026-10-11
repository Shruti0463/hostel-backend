package com.hostel.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Complaint {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String title;
    @Column(length = 1000)
    public String description;
    public String category;   // ELECTRICAL, PLUMBING, FURNITURE, CLEANING, OTHER
    public String location;   // block / room
    public String priority;   // LOW, MEDIUM, HIGH
    @Enumerated(EnumType.STRING)
    public Status status = Status.PENDING;
    public String remarks;
    public LocalDateTime createdAt = LocalDateTime.now();
    public LocalDateTime resolvedAt;
    @ManyToOne public User student;
    @ManyToOne public User assignedStaff;
}
