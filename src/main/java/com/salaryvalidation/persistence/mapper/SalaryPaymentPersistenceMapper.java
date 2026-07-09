package com.salaryvalidation.persistence.mapper;

import com.salaryvalidation.domain.salary.SalaryPayment;
import com.salaryvalidation.persistence.entity.EmployeeEntity;
import com.salaryvalidation.persistence.entity.SalaryPaymentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface SalaryPaymentPersistenceMapper {

    @Mapping(target = "employeeId", source = "employee", qualifiedByName = "employeeToEmployeeId")
    SalaryPayment toDomain(SalaryPaymentEntity entity);

    @Mapping(target = "employee", ignore = true)
    SalaryPaymentEntity toEntity(SalaryPayment domain);

    @Named("employeeToEmployeeId")
    default UUID mapEmployeeToEmployeeId(EmployeeEntity employee) {
        return employee != null ? employee.getId() : null;
    }
}
