package com.hospital_management.repo;

import com.hospital_management.models.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// DepartmentRepository.java
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByName(String name);
    List<Department> findByActiveTrue();
    Optional<Department> findByNameIgnoreCase(String name);


}