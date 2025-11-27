package com.knusdp.SmartLedger.exception;

public class AccountDuplicatedException extends RuntimeException {
    public AccountDuplicatedException(String message) {
        super(message);
    }
}
