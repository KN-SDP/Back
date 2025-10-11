package com.knusdp.SmartLedger.exception;

public class LedgerEntryNotFoundException extends RuntimeException {
    public LedgerEntryNotFoundException(String message) {
        super(message);
    }
}