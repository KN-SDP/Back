package com.knusdp.SmartLedger.exception;

public class EmailValidationError extends RuntimeException{
    public EmailValidationError(String message){
        super(message);
    }
}
