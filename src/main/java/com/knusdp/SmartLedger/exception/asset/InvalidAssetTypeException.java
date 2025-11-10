package com.knusdp.SmartLedger.exception.asset;

public class InvalidAssetTypeException extends RuntimeException {
    public InvalidAssetTypeException() {
        super("자산유형은 CASH, BANK중 하나여야 합니다.");
    }
}