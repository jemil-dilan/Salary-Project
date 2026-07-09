package com.salaryvalidation.application.employee;

import com.salaryvalidation.domain.employee.Employee;
import com.salaryvalidation.exception.DuplicateMatriculeException;
import com.salaryvalidation.exception.EmployeeNotFoundException;
import com.salaryvalidation.infrastructure.api.model.CreateEmployeeRequest;
import com.salaryvalidation.persistence.entity.EmployeeEntity;
import com.salaryvalidation.persistence.mapper.EmployeePersistenceMapper;
import com.salaryvalidation.persistence.repository.EmployeeJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateEmployeeUseCase {

    private final EmployeeJpaRepository employeeRepo;
    private final EmployeePersistenceMapper persistenceMapper;

    public UpdateEmployeeUseCase(EmployeeJpaRepository employeeRepo,
                                  EmployeePersistenceMapper persistenceMapper) {
        this.employeeRepo = employeeRepo;
        this.persistenceMapper = persistenceMapper;
    }

    @Transactional
    public Employee execute(UUID employeeId, CreateEmployeeRequest request) {
        EmployeeEntity entity = employeeRepo.findById(employeeId)
            .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        if (!entity.getMatricule().equals(request.getMatricule())
            && employeeRepo.existsByMatriculeAndIdNot(request.getMatricule(), employeeId)) {
            throw new DuplicateMatriculeException(request.getMatricule());
        }

        entity.setMatricule(request.getMatricule());
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setDepartment(request.getDepartment());
        entity.setPosition(request.getPosition());

        EmployeeEntity saved = employeeRepo.save(entity);
        return persistenceMapper.toDomain(saved);
    }
}
