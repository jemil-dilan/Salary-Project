package com.salaryvalidation.mapper;

import com.salaryvalidation.domain.employee.Employee;
import com.salaryvalidation.infrastructure.api.model.CreateEmployeeRequest;
import com.salaryvalidation.infrastructure.api.model.EmployeeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface EmployeeDtoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    Employee toDomain(CreateEmployeeRequest request);

    EmployeeResponse toResponse(Employee employee);
}
