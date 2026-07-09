package com.salaryvalidation.application.employee;

import com.salaryvalidation.domain.employee.Employee;
import com.salaryvalidation.domain.salary.SalaryPayment;
import com.salaryvalidation.exception.EmployeeInactiveException;
import com.salaryvalidation.exception.EmployeeNotFoundException;
import com.salaryvalidation.persistence.entity.EmployeeEntity;
import com.salaryvalidation.persistence.entity.SalaryPaymentEntity;
import com.salaryvalidation.persistence.mapper.EmployeePersistenceMapper;
import com.salaryvalidation.persistence.mapper.SalaryPaymentPersistenceMapper;
import com.salaryvalidation.persistence.repository.EmployeeJpaRepository;
import com.salaryvalidation.persistence.repository.SalaryPaymentJpaRepository;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.ZoneOffset;

@Service
public class SearchByMatriculeUseCase {

    private final EmployeeJpaRepository employeeRepo;
    private final SalaryPaymentJpaRepository salaryPaymentRepo;
    private final EmployeePersistenceMapper employeeMapper;
    private final SalaryPaymentPersistenceMapper salaryPaymentMapper;

    public SearchByMatriculeUseCase(EmployeeJpaRepository employeeRepo,
                                     SalaryPaymentJpaRepository salaryPaymentRepo,
                                     EmployeePersistenceMapper employeeMapper,
                                     SalaryPaymentPersistenceMapper salaryPaymentMapper) {
        this.employeeRepo = employeeRepo;
        this.salaryPaymentRepo = salaryPaymentRepo;
        this.employeeMapper = employeeMapper;
        this.salaryPaymentMapper = salaryPaymentMapper;
    }

    public record EmployeeWithCurrentPaymentResponse(Employee employee, SalaryPayment salaryPayment) {
    }

    public EmployeeWithCurrentPaymentResponse execute(String matricule) {
        EmployeeEntity entity = employeeRepo.findByMatricule(matricule)
            .orElseThrow(() -> new EmployeeNotFoundException(matricule));

        if (!entity.isActive()) {
            throw new EmployeeInactiveException(matricule);
        }

        Employee employee = employeeMapper.toDomain(entity);

        YearMonth currentPeriod = YearMonth.now(ZoneOffset.UTC);
        int currentMonth = currentPeriod.getMonthValue();
        int currentYear = currentPeriod.getYear();

        SalaryPayment salaryPayment = salaryPaymentRepo
            .findByEmployeeIdAndMonthAndYear(employee.getId(), currentMonth, currentYear)
            .map(salaryPaymentMapper::toDomain)
            .orElse(null);

        return new EmployeeWithCurrentPaymentResponse(employee, salaryPayment);
    }
}
