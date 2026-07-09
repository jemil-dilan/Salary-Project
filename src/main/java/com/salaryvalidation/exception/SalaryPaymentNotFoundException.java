package com.salaryvalidation.exception;

import java.util.UUID;

public class SalaryPaymentNotFoundException extends RuntimeException {

    public SalaryPaymentNotFoundException(UUID id) {
        super("Salary payment not found with id: " + id);
    }

    public SalaryPaymentNotFoundException(int month, int year) {
        super("No salary payment found for period " + month + "/" + year);
    }
}
