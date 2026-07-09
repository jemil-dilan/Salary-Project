package com.salaryvalidation.exception;

public class DuplicateMatriculeException extends RuntimeException {

    public DuplicateMatriculeException(String matricule) {
        super("An employee with matricule '" + matricule + "' already exists");
    }
}
