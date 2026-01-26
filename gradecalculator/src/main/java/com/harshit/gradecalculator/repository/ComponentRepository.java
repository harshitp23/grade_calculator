package com.harshit.gradecalculator.repository;

import com.harshit.gradecalculator.model.Component;
import com.harshit.gradecalculator.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComponentRepository extends JpaRepository<Component, Integer> {
    List<Component> findBySubject(Subject subject);
}
