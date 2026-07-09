package com.salaryvalidation.persistence.repository;

import com.salaryvalidation.persistence.entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeJpaRepository extends JpaRepository<EmployeeEntity, UUID> {

    Optional<EmployeeEntity> findByMatricule(String matricule);

    boolean existsByMatricule(String matricule);

    boolean existsByMatriculeAndIdNot(String matricule, UUID id);
}
