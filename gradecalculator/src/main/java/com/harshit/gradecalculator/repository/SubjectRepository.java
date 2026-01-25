package com.harshit.gradecalculator.repository;

import com.harshit.gradecalculator.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Integer> {

    // Fetch all subjects for a specific user
    // SQL: "SELECT * FROM Subjects WHERE user_id = ?"
    List<Subject> findByUser_UserId(Integer userId);
}