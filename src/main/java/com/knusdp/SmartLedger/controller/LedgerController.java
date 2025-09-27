package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.CreateAccountDto;
import com.knusdp.SmartLedger.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ledger")
public class LedgerController {
    private final LedgerService ledgerService;

    @PostMapping
    public ResponseEntity<?> createLedgerEntry(@RequestBody CreateAccountDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());



        return ResponseEntity.noContent().build();
    }
}
