package com.salaryvalidation.application.employee;

import com.salaryvalidation.exception.EmployeeNotFoundException;
import com.salaryvalidation.persistence.entity.EmployeeEntity;
import com.salaryvalidation.persistence.repository.EmployeeJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteEmployeeUseCase {

    private final EmployeeJpaRepository employeeRepo;

    public DeleteEmployeeUseCase(EmployeeJpaRepository employeeRepo) {
        this.employeeRepo = employeeRepo;
    }

    @Transactional
    public void execute(UUID employeeId) {
        EmployeeEntity entity = employeeRepo.findById(employeeId)
            .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        entity.setActive(false);
        employeeRepo.save(entity);
    }
}
