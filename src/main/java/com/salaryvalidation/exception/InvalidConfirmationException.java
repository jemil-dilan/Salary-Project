package com.salaryvalidation.exception;

public class InvalidConfirmationException extends RuntimeException {

    private final String errorCode;

    public InvalidConfirmationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
