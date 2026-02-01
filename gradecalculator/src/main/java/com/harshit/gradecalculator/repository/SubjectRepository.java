package com.harshit.gradecalculator.repository;

import com.harshit.gradecalculator.model.Subject;
import com.harshit.gradecalculator.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // <--- Don't forget this import!
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Integer> {
    
    // 👇 ADD THIS LINE 👇
    List<Subject> findByUser(User user);

    Optional<Subject> findByUserAndSubjectCode(User user, String subjectCode);

}
