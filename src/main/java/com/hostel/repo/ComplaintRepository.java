package com.hostel.repo;

import com.hostel.model.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByStudentOrderByCreatedAtDesc(User student);
    List<Complaint> findByAssignedStaffOrderByCreatedAtDesc(User staff);
    List<Complaint> findAllByOrderByCreatedAtDesc();
    long countByStatus(Status status);
}
