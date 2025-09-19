package com.knusdp.SmartLedger;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class SmartLedgerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartLedgerApplication.class, args);
    }

}
