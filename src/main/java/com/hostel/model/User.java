package com.hostel.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String name;
    @Column(unique = true, nullable = false)
    public String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String password;
    @Enumerated(EnumType.STRING)
    public Role role;
    public String roomNo;        // students
    public String specialization; // staff (Electrician, Plumber...)
}
