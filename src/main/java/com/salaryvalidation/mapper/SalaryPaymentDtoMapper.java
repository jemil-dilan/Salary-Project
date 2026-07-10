package com.salaryvalidation.mapper;

import com.salaryvalidation.domain.salary.SalaryPayment;
import com.salaryvalidation.infrastructure.api.model.ConfirmationResponse;
import com.salaryvalidation.infrastructure.api.model.EmployeeRef;
import com.salaryvalidation.infrastructure.api.model.SalaryPaymentSimple;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface SalaryPaymentDtoMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    SalaryPayment toDomain(com.salaryvalidation.infrastructure.api.model.SalaryPaymentResponse response);

    @Mapping(target = "id", source = "payment.id")
    @Mapping(target = "employee", source = "employee")
    @Mapping(target = "month", source = "payment.month")
    @Mapping(target = "year", source = "payment.year")
    @Mapping(target = "status", source = "payment.status")
    @Mapping(target = "confirmationDate", source = "payment.confirmationDate")
    @Mapping(target = "expectedAmount", source = "payment.expectedAmount", qualifiedByName = "bigDecimalToDouble")
    @Mapping(target = "receivedAmount", source = "payment.receivedAmount", qualifiedByName = "bigDecimalToDouble")
    com.salaryvalidation.infrastructure.api.model.SalaryPaymentResponse toResponse(SalaryPayment payment,
                                                                                    EmployeeRef employee);

    @Mapping(target = "expectedAmount", source = "expectedAmount", qualifiedByName = "bigDecimalToDouble")
    @Mapping(target = "receivedAmount", source = "receivedAmount", qualifiedByName = "bigDecimalToDouble")
    ConfirmationResponse toConfirmationResponse(SalaryPayment payment);

    @Mapping(target = "canConfirm", ignore = true)
    @Mapping(target = "expectedAmount", source = "expectedAmount", qualifiedByName = "bigDecimalToDouble")
    @Mapping(target = "receivedAmount", source = "receivedAmount", qualifiedByName = "bigDecimalToDouble")
    SalaryPaymentSimple toSimpleResponse(SalaryPayment payment);

    @Named("bigDecimalToDouble")
    default Double bigDecimalToDouble(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
