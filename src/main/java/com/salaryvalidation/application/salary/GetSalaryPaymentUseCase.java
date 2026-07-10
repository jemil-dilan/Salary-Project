package com.salaryvalidation.application.salary;

import com.salaryvalidation.domain.employee.Employee;
import com.salaryvalidation.domain.salary.SalaryPayment;
import com.salaryvalidation.exception.SalaryPaymentNotFoundException;
import com.salaryvalidation.persistence.entity.SalaryPaymentEntity;
import com.salaryvalidation.persistence.mapper.EmployeePersistenceMapper;
import com.salaryvalidation.persistence.mapper.SalaryPaymentPersistenceMapper;
import com.salaryvalidation.persistence.repository.SalaryPaymentJpaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetSalaryPaymentUseCase {

    private final SalaryPaymentJpaRepository salaryPaymentRepo;
    private final SalaryPaymentPersistenceMapper salaryPaymentMapper;
    private final EmployeePersistenceMapper employeeMapper;

    public GetSalaryPaymentUseCase(SalaryPaymentJpaRepository salaryPaymentRepo,
                                    SalaryPaymentPersistenceMapper salaryPaymentMapper,
                                    EmployeePersistenceMapper employeeMapper) {
        this.salaryPaymentRepo = salaryPaymentRepo;
        this.salaryPaymentMapper = salaryPaymentMapper;
        this.employeeMapper = employeeMapper;
    }

    public record SalaryPaymentWithEmployeeResponse(SalaryPayment salaryPayment, Employee employee) {
    }

    public SalaryPaymentWithEmployeeResponse execute(UUID paymentId) {
        SalaryPaymentEntity entity = salaryPaymentRepo.findById(paymentId)
            .orElseThrow(() -> new SalaryPaymentNotFoundException(paymentId));

        SalaryPayment salaryPayment = salaryPaymentMapper.toDomain(entity);
        Employee employee = employeeMapper.toDomain(entity.getEmployee());

        return new SalaryPaymentWithEmployeeResponse(salaryPayment, employee);
    }
}
