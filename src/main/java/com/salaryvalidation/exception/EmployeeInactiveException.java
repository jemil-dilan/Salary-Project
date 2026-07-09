package com.salaryvalidation.exception;

public class EmployeeInactiveException extends RuntimeException {

    public EmployeeInactiveException(String matricule) {
        super("Employee account is not active: " + matricule);
    }
}
