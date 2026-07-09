package com.salaryvalidation.persistence.mapper;

import com.salaryvalidation.domain.employee.Employee;
import com.salaryvalidation.persistence.entity.EmployeeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeePersistenceMapper {

    EmployeeEntity toEntity(Employee employee);

    Employee toDomain(EmployeeEntity entity);
}
