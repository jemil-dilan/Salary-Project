package com.salaryvalidation.persistence.repository;

import com.salaryvalidation.persistence.entity.SalaryPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalaryPaymentJpaRepository extends JpaRepository<SalaryPaymentEntity, UUID> {

    Optional<SalaryPaymentEntity> findByEmployeeIdAndMonthAndYear(UUID employeeId, int month, int year);

    boolean existsByEmployeeIdAndMonthAndYear(UUID employeeId, int month, int year);
}
