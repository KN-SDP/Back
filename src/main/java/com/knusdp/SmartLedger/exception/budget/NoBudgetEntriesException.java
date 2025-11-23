package com.knusdp.SmartLedger.exception.budget;

public class NoBudgetEntriesException extends RuntimeException {
    public NoBudgetEntriesException(String message) {
        super(message);
    }
}
