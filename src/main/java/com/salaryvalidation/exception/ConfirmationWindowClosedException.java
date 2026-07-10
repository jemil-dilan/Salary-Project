package com.salaryvalidation.exception;

public class ConfirmationWindowClosedException extends RuntimeException {

    public ConfirmationWindowClosedException() {
        super("Confirmation window is closed. Confirmations are only allowed between the 25th of the current month and the 5th of the next month.");
    }
}
