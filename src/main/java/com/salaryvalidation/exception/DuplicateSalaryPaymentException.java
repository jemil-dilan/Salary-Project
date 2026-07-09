package com.salaryvalidation.exception;

import java.util.UUID;

public class DuplicateSalaryPaymentException extends RuntimeException {

    public DuplicateSalaryPaymentException(UUID employeeId, int month, int year) {
        super("A salary payment already exists for employee " + employeeId
              + " for period " + month + "/" + year);
    }
}
