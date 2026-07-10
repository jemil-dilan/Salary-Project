package com.salaryvalidation.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeNotFound(EmployeeNotFoundException ex,
                                                                 HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(EmployeeInactiveException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeInactive(EmployeeInactiveException ex,
                                                                 HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "EMPLOYEE_INACTIVE", ex.getMessage(), request);
    }

    @ExceptionHandler(SalaryPaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(SalaryPaymentNotFoundException ex,
                                                                HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(NoCurrentPaymentException.class)
    public ResponseEntity<ErrorResponse> handleNoCurrentPayment(NoCurrentPaymentException ex,
                                                                 HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "NO_CURRENT_PAYMENT", ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateMatriculeException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateMatricule(DuplicateMatriculeException ex,
                                                                    HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "DUPLICATE_MATRICULE", ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateSalaryPaymentException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateSalaryPayment(DuplicateSalaryPaymentException ex,
                                                                        HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "DUPLICATE_SALARY_PAYMENT", ex.getMessage(), request);
    }

    @ExceptionHandler(AlreadyConfirmedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyConfirmed(AlreadyConfirmedException ex,
                                                                 HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "ALREADY_CONFIRMED", ex.getMessage(), request);
    }

    @ExceptionHandler(ConfirmationWindowClosedException.class)
    public ResponseEntity<ErrorResponse> handleConfirmationWindowClosed(ConfirmationWindowClosedException ex,
                                                                         HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "CONFIRMATION_WINDOW_CLOSED", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidConfirmationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidConfirmation(InvalidConfirmationException ex,
                                                                     HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getErrorCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                                 HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                           HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse("Validation failed");
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                     HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                             "An unexpected error occurred", request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus httpStatus, String errorCode,
                                                         String message, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(httpStatus.value());
        error.setErrorCode(errorCode);
        error.setMessage(message);
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(httpStatus).body(error);
    }
}
