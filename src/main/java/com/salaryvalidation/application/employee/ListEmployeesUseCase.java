package com.salaryvalidation.application.employee;

import com.salaryvalidation.domain.employee.Employee;
import com.salaryvalidation.persistence.entity.EmployeeEntity;
import com.salaryvalidation.persistence.mapper.EmployeePersistenceMapper;
import com.salaryvalidation.persistence.repository.EmployeeJpaRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ListEmployeesUseCase {

    private final EmployeeJpaRepository employeeRepo;
    private final EmployeePersistenceMapper persistenceMapper;

    public ListEmployeesUseCase(EmployeeJpaRepository employeeRepo,
                                 EmployeePersistenceMapper persistenceMapper) {
        this.employeeRepo = employeeRepo;
        this.persistenceMapper = persistenceMapper;
    }

    public Page<Employee> execute(Boolean active, String department, Pageable pageable) {
        EmployeeEntity probe = new EmployeeEntity();
        ExampleMatcher matcher = ExampleMatcher.matching()
            .withIgnorePaths("id", "matricule", "firstName", "lastName", "position");

        if (active != null) {
            probe.setActive(active);
            matcher = matcher.withMatcher("active", ExampleMatcher.GenericPropertyMatchers.exact());
        }
        if (department != null) {
            probe.setDepartment(department);
            matcher = matcher.withMatcher("department", ExampleMatcher.GenericPropertyMatchers.exact());
        }

        Example<EmployeeEntity> example = Example.of(probe, matcher);
        return employeeRepo.findAll(example, pageable)
            .map(persistenceMapper::toDomain);
    }
}
