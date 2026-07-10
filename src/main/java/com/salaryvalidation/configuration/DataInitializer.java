package com.salaryvalidation.configuration;

import com.salaryvalidation.domain.salary.PaymentStatus;
import com.salaryvalidation.persistence.entity.EmployeeEntity;
import com.salaryvalidation.persistence.entity.SalaryPaymentEntity;
import com.salaryvalidation.persistence.repository.EmployeeJpaRepository;
import com.salaryvalidation.persistence.repository.SalaryPaymentJpaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EmployeeJpaRepository employeeRepo;
    private final SalaryPaymentJpaRepository salaryPaymentRepo;

    public DataInitializer(EmployeeJpaRepository employeeRepo,
                           SalaryPaymentJpaRepository salaryPaymentRepo) {
        this.employeeRepo = employeeRepo;
        this.salaryPaymentRepo = salaryPaymentRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (employeeRepo.count() > 0) {
            return;
        }

        YearMonth current = YearMonth.now(ZoneOffset.UTC);
        int month = current.getMonthValue();
        int year = current.getYear();

        EmployeeEntity emp1 = createEmployee("EMP-001", "John", "Doe",
            "Engineering", "Software Developer", true);
        EmployeeEntity emp2 = createEmployee("EMP-002", "Jane", "Smith",
            "Marketing", "Marketing Manager", true);
        EmployeeEntity emp3 = createEmployee("EMP-003", "Bob", "Johnson",
            "Finance", "Financial Analyst", true);
        EmployeeEntity emp4 = createEmployee("EMP-004", "Alice", "Williams",
            "Human Resources", "HR Coordinator", true);
        createEmployee("EMP-005", "Charlie", "Brown",
            "Sales", "Sales Representative", false);

        createSalaryPayment(emp1, month, year, new BigDecimal("150000.00"),
            null, PaymentStatus.PENDING, null);

        createSalaryPayment(emp2, month, year, new BigDecimal("180000.00"),
            new BigDecimal("180000.00"), PaymentStatus.FULLY_RECEIVED,
            LocalDateTime.now(ZoneOffset.UTC).minusDays(3));

        createSalaryPayment(emp3, month, year, new BigDecimal("120000.00"),
            new BigDecimal("80000.00"), PaymentStatus.PARTIALLY_RECEIVED,
            LocalDateTime.now(ZoneOffset.UTC).minusDays(5));
    }

    private EmployeeEntity createEmployee(String matricule, String firstName, String lastName,
                                           String department, String position, boolean active) {
        EmployeeEntity entity = new EmployeeEntity();
        entity.setMatricule(matricule);
        entity.setFirstName(firstName);
        entity.setLastName(lastName);
        entity.setDepartment(department);
        entity.setPosition(position);
        entity.setActive(active);
        return employeeRepo.save(entity);
    }

    private void createSalaryPayment(EmployeeEntity employee, int month, int year,
                                      BigDecimal expectedAmount, BigDecimal receivedAmount,
                                      PaymentStatus status, LocalDateTime confirmationDate) {
        SalaryPaymentEntity entity = new SalaryPaymentEntity();
        entity.setEmployee(employee);
        entity.setMonth(month);
        entity.setYear(year);
        entity.setExpectedAmount(expectedAmount);
        entity.setReceivedAmount(receivedAmount);
        entity.setStatus(status);
        entity.setConfirmationDate(confirmationDate);
        salaryPaymentRepo.save(entity);
    }
}
