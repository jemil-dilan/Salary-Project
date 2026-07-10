package com.salaryvalidation.application.salary;

import com.salaryvalidation.configuration.ConfirmationWindow;
import com.salaryvalidation.domain.salary.PaymentStatus;
import com.salaryvalidation.domain.salary.SalaryPayment;
import com.salaryvalidation.exception.AlreadyConfirmedException;
import com.salaryvalidation.exception.ConfirmationWindowClosedException;
import com.salaryvalidation.exception.EmployeeInactiveException;
import com.salaryvalidation.exception.EmployeeNotFoundException;
import com.salaryvalidation.exception.InvalidConfirmationException;
import com.salaryvalidation.exception.NoCurrentPaymentException;
import com.salaryvalidation.infrastructure.api.model.ConfirmSalaryRequest;
import com.salaryvalidation.persistence.entity.EmployeeEntity;
import com.salaryvalidation.persistence.entity.SalaryPaymentEntity;
import com.salaryvalidation.persistence.mapper.SalaryPaymentPersistenceMapper;
import com.salaryvalidation.persistence.repository.EmployeeJpaRepository;
import com.salaryvalidation.persistence.repository.SalaryPaymentJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;

@Service
public class ConfirmSalaryUseCase {

    private final EmployeeJpaRepository employeeRepo;
    private final SalaryPaymentJpaRepository salaryPaymentRepo;
    private final SalaryPaymentPersistenceMapper mapper;
    private final ConfirmationWindow confirmationWindow;
    private final Clock clock;

    public ConfirmSalaryUseCase(EmployeeJpaRepository employeeRepo,
                                 SalaryPaymentJpaRepository salaryPaymentRepo,
                                 SalaryPaymentPersistenceMapper mapper,
                                 ConfirmationWindow confirmationWindow,
                                 Clock clock) {
        this.employeeRepo = employeeRepo;
        this.salaryPaymentRepo = salaryPaymentRepo;
        this.mapper = mapper;
        this.confirmationWindow = confirmationWindow;
        this.clock = clock;
    }

    @Transactional
    public SalaryPayment execute(String matricule, ConfirmSalaryRequest request) {
        EmployeeEntity employee = employeeRepo.findByMatricule(matricule)
            .orElseThrow(() -> new EmployeeNotFoundException(matricule));

        if (!employee.isActive()) {
            throw new EmployeeInactiveException(matricule);
        }

        YearMonth currentPeriod = YearMonth.now(ZoneOffset.UTC);
        int currentMonth = currentPeriod.getMonthValue();
        int currentYear = currentPeriod.getYear();

        SalaryPaymentEntity payment = salaryPaymentRepo
            .findByEmployeeIdAndMonthAndYear(employee.getId(), currentMonth, currentYear)
            .orElseThrow(() -> new NoCurrentPaymentException(matricule));

        if (payment.getStatus() == PaymentStatus.FULLY_RECEIVED) {
            throw new AlreadyConfirmedException(payment.getStatus());
        }

        if (payment.getStatus() == PaymentStatus.PENDING && !confirmationWindow.isOpen()) {
            throw new ConfirmationWindowClosedException();
        }

        Double requestAmount = request.getReceivedAmount();
        BigDecimal newReceivedAmount;

        if (requestAmount == null) {
            newReceivedAmount = BigDecimal.ZERO;
        } else {
            newReceivedAmount = BigDecimal.valueOf(requestAmount);
        }

        if (payment.getReceivedAmount() != null
                && newReceivedAmount.compareTo(payment.getReceivedAmount()) < 0) {
            throw new InvalidConfirmationException("INVALID_AMOUNT_DECREASE",
                "Received amount cannot be decreased from " + payment.getReceivedAmount()
                    + " to " + newReceivedAmount);
        }

        PaymentStatus derivedStatus;
        BigDecimal expectedAmount = payment.getExpectedAmount();

        if (newReceivedAmount.compareTo(BigDecimal.ZERO) == 0) {
            derivedStatus = PaymentStatus.NOT_RECEIVED;
        } else if (newReceivedAmount.compareTo(expectedAmount) < 0) {
            derivedStatus = PaymentStatus.PARTIALLY_RECEIVED;
        } else {
            derivedStatus = PaymentStatus.FULLY_RECEIVED;
        }

        payment.setReceivedAmount(newReceivedAmount);
        payment.setStatus(derivedStatus);
        payment.setConfirmationDate(LocalDateTime.now(clock));

        SalaryPaymentEntity saved = salaryPaymentRepo.save(payment);
        return mapper.toDomain(saved);
    }
}
