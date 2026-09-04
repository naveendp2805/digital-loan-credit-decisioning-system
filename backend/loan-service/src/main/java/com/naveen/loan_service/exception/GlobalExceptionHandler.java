package com.naveen.loan_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.ConstraintViolationException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(LoanNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLoanNotFound(
            LoanNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        new ErrorResponse(
                                404,
                                ex.getMessage(),
                                LocalDateTime.now()
                        )
                );
    }


    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(
            CustomerNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        new ErrorResponse(
                                404,
                                ex.getMessage(),
                                LocalDateTime.now()
                        )
                );
    }


    @ExceptionHandler(InvalidLoanStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatus(
            InvalidLoanStatusException ex) {

        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorResponse(
                                400,
                                ex.getMessage(),
                                LocalDateTime.now()
                        )
                );
    }


    @ExceptionHandler(CustomerServiceException.class)
    public ResponseEntity<ErrorResponse> handleCustomerService(
            CustomerServiceException ex) {

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(
                        new ErrorResponse(
                                503,
                                ex.getMessage(),
                                LocalDateTime.now()
                        )
                );
    }


    @ExceptionHandler(CreditDecisionException.class)
    public ResponseEntity<ErrorResponse> handleCreditDecision(
            CreditDecisionException ex) {

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(
                        new ErrorResponse(
                                503,
                                ex.getMessage(),
                                LocalDateTime.now()
                        )
                );
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>>
    handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors =
                new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return ResponseEntity
                .badRequest()
                .body(errors);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse>
    handleConstraintViolation(
            ConstraintViolationException ex) {

        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorResponse(
                                400,
                                ex.getMessage(),
                                LocalDateTime.now()
                        )
                );
    }
}