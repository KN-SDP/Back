package com.knusdp.SmartLedger.exception.auth;

public class AccountDeletedException extends RuntimeException {
    public AccountDeletedException(String message) {
        super(message);
    }
}
