package com.salaryvalidation.application.employee;

import com.salaryvalidation.domain.employee.Employee;
import com.salaryvalidation.exception.EmployeeNotFoundException;
import com.salaryvalidation.persistence.mapper.EmployeePersistenceMapper;
import com.salaryvalidation.persistence.repository.EmployeeJpaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetEmployeeUseCase {

    private final EmployeeJpaRepository employeeRepo;
    private final EmployeePersistenceMapper persistenceMapper;

    public GetEmployeeUseCase(EmployeeJpaRepository employeeRepo,
                               EmployeePersistenceMapper persistenceMapper) {
        this.employeeRepo = employeeRepo;
        this.persistenceMapper = persistenceMapper;
    }

    public Employee execute(UUID employeeId) {
        var entity = employeeRepo.findById(employeeId)
            .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        return persistenceMapper.toDomain(entity);
    }
}
