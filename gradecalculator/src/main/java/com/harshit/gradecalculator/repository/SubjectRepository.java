package com.harshit.gradecalculator.repository;

import com.harshit.gradecalculator.model.Subject;
import com.harshit.gradecalculator.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Integer> {
    // Required by SimulatorController and SubjectController
    List<Subject> findByUser(User user); 

    // Required by TargetCalcController
    List<Subject> findByUserAndStatus(User user, String status); 

    Optional<Subject> findByUserAndSubjectCode(User user, String subjectCode);
}
