package com.harshit.gradecalculator.repository;

import com.harshit.gradecalculator.model.Component;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComponentRepository extends JpaRepository<Component, Integer> {
    // We don't need custom queries yet, standard ones are fine!
}