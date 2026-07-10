package com.salaryvalidation.application.salary;

import com.salaryvalidation.domain.salary.PaymentStatus;
import com.salaryvalidation.domain.salary.SalaryPayment;
import com.salaryvalidation.exception.DuplicateSalaryPaymentException;
import com.salaryvalidation.exception.EmployeeNotFoundException;
import com.salaryvalidation.infrastructure.api.model.CreateSalaryPaymentRequest;
import com.salaryvalidation.persistence.entity.EmployeeEntity;
import com.salaryvalidation.persistence.entity.SalaryPaymentEntity;
import com.salaryvalidation.persistence.mapper.SalaryPaymentPersistenceMapper;
import com.salaryvalidation.persistence.repository.EmployeeJpaRepository;
import com.salaryvalidation.persistence.repository.SalaryPaymentJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CreateSalaryPaymentUseCase {

    private final EmployeeJpaRepository employeeRepo;
    private final SalaryPaymentJpaRepository salaryPaymentRepo;
    private final SalaryPaymentPersistenceMapper mapper;

    public CreateSalaryPaymentUseCase(EmployeeJpaRepository employeeRepo,
                                       SalaryPaymentJpaRepository salaryPaymentRepo,
                                       SalaryPaymentPersistenceMapper mapper) {
        this.employeeRepo = employeeRepo;
        this.salaryPaymentRepo = salaryPaymentRepo;
        this.mapper = mapper;
    }

    @Transactional
    public SalaryPayment execute(CreateSalaryPaymentRequest request) {
        EmployeeEntity employee = employeeRepo.findById(request.getEmployeeId())
            .orElseThrow(() -> new EmployeeNotFoundException(request.getEmployeeId()));

        if (!employee.isActive()) {
            throw new EmployeeNotFoundException(request.getEmployeeId());
        }

        if (salaryPaymentRepo.existsByEmployeeIdAndMonthAndYear(
                request.getEmployeeId(), request.getMonth(), request.getYear())) {
            throw new DuplicateSalaryPaymentException(
                request.getEmployeeId(), request.getMonth(), request.getYear());
        }

        SalaryPaymentEntity entity = new SalaryPaymentEntity();
        entity.setEmployee(employee);
        entity.setMonth(request.getMonth());
        entity.setYear(request.getYear());
        entity.setExpectedAmount(BigDecimal.valueOf(request.getExpectedAmount()));
        entity.setStatus(PaymentStatus.PENDING);

        SalaryPaymentEntity saved = salaryPaymentRepo.save(entity);
        return mapper.toDomain(saved);
    }
}
