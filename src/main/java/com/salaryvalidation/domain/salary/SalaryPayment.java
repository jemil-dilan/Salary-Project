package com.salaryvalidation.domain.salary;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class SalaryPayment {

    private UUID id;
    private UUID employeeId;
    private int month;
    private int year;
    private BigDecimal expectedAmount;
    private BigDecimal receivedAmount;
    private PaymentStatus status;
    private LocalDateTime confirmationDate;

    public SalaryPayment() {
    }

    public SalaryPayment(UUID id, UUID employeeId, int month, int year,
                         BigDecimal expectedAmount, BigDecimal receivedAmount,
                         PaymentStatus status, LocalDateTime confirmationDate) {
        this.id = id;
        this.employeeId = employeeId;
        this.month = month;
        this.year = year;
        this.expectedAmount = expectedAmount;
        this.receivedAmount = receivedAmount;
        this.status = status;
        this.confirmationDate = confirmationDate;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(UUID employeeId) {
        this.employeeId = employeeId;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public BigDecimal getExpectedAmount() {
        return expectedAmount;
    }

    public void setExpectedAmount(BigDecimal expectedAmount) {
        this.expectedAmount = expectedAmount;
    }

    public BigDecimal getReceivedAmount() {
        return receivedAmount;
    }

    public void setReceivedAmount(BigDecimal receivedAmount) {
        this.receivedAmount = receivedAmount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getConfirmationDate() {
        return confirmationDate;
    }

    public void setConfirmationDate(LocalDateTime confirmationDate) {
        this.confirmationDate = confirmationDate;
    }
}
