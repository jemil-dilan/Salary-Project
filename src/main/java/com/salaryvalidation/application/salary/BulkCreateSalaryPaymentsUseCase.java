package com.salaryvalidation.application.salary;

import com.salaryvalidation.domain.salary.PaymentStatus;
import com.salaryvalidation.domain.salary.SalaryPayment;
import com.salaryvalidation.exception.DuplicateSalaryPaymentException;
import com.salaryvalidation.exception.EmployeeNotFoundException;
import com.salaryvalidation.exception.InvalidConfirmationException;
import com.salaryvalidation.infrastructure.api.model.BulkCreateSalaryPaymentRequest;
import com.salaryvalidation.persistence.entity.EmployeeEntity;
import com.salaryvalidation.persistence.entity.SalaryPaymentEntity;
import com.salaryvalidation.persistence.mapper.SalaryPaymentPersistenceMapper;
import com.salaryvalidation.persistence.repository.EmployeeJpaRepository;
import com.salaryvalidation.persistence.repository.SalaryPaymentJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class BulkCreateSalaryPaymentsUseCase {

    private final EmployeeJpaRepository employeeRepo;
    private final SalaryPaymentJpaRepository salaryPaymentRepo;
    private final SalaryPaymentPersistenceMapper mapper;

    public BulkCreateSalaryPaymentsUseCase(EmployeeJpaRepository employeeRepo,
                                            SalaryPaymentJpaRepository salaryPaymentRepo,
                                            SalaryPaymentPersistenceMapper mapper) {
        this.employeeRepo = employeeRepo;
        this.salaryPaymentRepo = salaryPaymentRepo;
        this.mapper = mapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<SalaryPayment> execute(BulkCreateSalaryPaymentRequest request) {
        int month = request.getMonth();
        int year = request.getYear();
        var payments = request.getPayments();

        Set<UUID> seenEmployeeIds = new HashSet<>();
        for (var entry : payments) {
            if (!seenEmployeeIds.add(entry.getEmployeeId())) {
                throw new InvalidConfirmationException("DUPLICATE_EMPLOYEE_IN_BULK",
                    "Duplicate employee in bulk request: " + entry.getEmployeeId());
            }
        }

        List<SalaryPaymentEntity> entities = new ArrayList<>();

        for (var entry : payments) {
            EmployeeEntity employee = employeeRepo.findById(entry.getEmployeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(entry.getEmployeeId()));

            if (!employee.isActive()) {
                throw new EmployeeNotFoundException(entry.getEmployeeId());
            }

            if (salaryPaymentRepo.existsByEmployeeIdAndMonthAndYear(
                    entry.getEmployeeId(), month, year)) {
                throw new DuplicateSalaryPaymentException(entry.getEmployeeId(), month, year);
            }

            SalaryPaymentEntity entity = new SalaryPaymentEntity();
            entity.setEmployee(employee);
            entity.setMonth(month);
            entity.setYear(year);
            entity.setExpectedAmount(BigDecimal.valueOf(entry.getExpectedAmount()));
            entity.setStatus(PaymentStatus.PENDING);
            entities.add(entity);
        }

        List<SalaryPaymentEntity> saved = salaryPaymentRepo.saveAll(entities);
        return saved.stream().map(mapper::toDomain).toList();
    }
}
