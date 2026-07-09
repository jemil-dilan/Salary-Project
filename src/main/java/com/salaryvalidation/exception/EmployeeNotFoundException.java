package com.salaryvalidation.exception;

import java.util.UUID;

public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(UUID id) {
        super("Employee not found with id: " + id);
    }

    public EmployeeNotFoundException(String matricule) {
        super("Employee not found with matricule: " + matricule);
    }
}
