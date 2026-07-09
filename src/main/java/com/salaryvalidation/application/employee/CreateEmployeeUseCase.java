package com.salaryvalidation.application.employee;

import com.salaryvalidation.domain.employee.Employee;
import com.salaryvalidation.exception.DuplicateMatriculeException;
import com.salaryvalidation.mapper.EmployeeDtoMapper;
import com.salaryvalidation.persistence.entity.EmployeeEntity;
import com.salaryvalidation.persistence.mapper.EmployeePersistenceMapper;
import com.salaryvalidation.persistence.repository.EmployeeJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateEmployeeUseCase {

    private final EmployeeJpaRepository employeeRepo;
    private final EmployeePersistenceMapper persistenceMapper;
    private final EmployeeDtoMapper dtoMapper;

    public CreateEmployeeUseCase(EmployeeJpaRepository employeeRepo,
                                  EmployeePersistenceMapper persistenceMapper,
                                  EmployeeDtoMapper dtoMapper) {
        this.employeeRepo = employeeRepo;
        this.persistenceMapper = persistenceMapper;
        this.dtoMapper = dtoMapper;
    }

    @Transactional
    public Employee execute(com.salaryvalidation.infrastructure.api.model.CreateEmployeeRequest request) {
        if (employeeRepo.existsByMatricule(request.getMatricule())) {
            throw new DuplicateMatriculeException(request.getMatricule());
        }

        Employee employee = dtoMapper.toDomain(request);
        employee.setActive(true);

        EmployeeEntity saved = employeeRepo.save(persistenceMapper.toEntity(employee));
        return persistenceMapper.toDomain(saved);
    }
}
