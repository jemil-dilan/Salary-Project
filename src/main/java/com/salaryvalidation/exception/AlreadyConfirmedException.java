package com.salaryvalidation.exception;

import com.salaryvalidation.domain.salary.PaymentStatus;

public class AlreadyConfirmedException extends RuntimeException {

    public AlreadyConfirmedException(PaymentStatus currentStatus) {
        super("Salary payment has already been confirmed. Current status: " + currentStatus);
    }
}
