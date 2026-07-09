package com.salaryvalidation.exception;

public class NoCurrentPaymentException extends RuntimeException {

    public NoCurrentPaymentException(String matricule) {
        super("No salary payment for current period for employee: " + matricule);
    }
}
