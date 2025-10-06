package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.CreateAccountDto;
import com.knusdp.SmartLedger.dto.LedgerResponseDto;
import com.knusdp.SmartLedger.entity.TransactionType;
import com.knusdp.SmartLedger.service.AccountBookService;
import com.knusdp.SmartLedger.service.FindInFoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ledger")
public class LedgerController {
    private final AccountBookService accountBookService;
    private final FindInFoService findInFoService;

    @PostMapping
    public ResponseEntity<?> createLedgerEntry(@RequestBody CreateAccountDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        accountBookService.createLedgerEntry(userId, dto);

        return ResponseEntity.noContent().build();
    }

    @Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<List<LedgerResponseDto>> getLedgerEntriesByCategory(
            @RequestParam("category") String categoryName
    ) {
        // 토큰에서 현재 사용자 ID 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        // 서비스 호출
        List<LedgerResponseDto> response = findInFoService.findEntriesByCategory(userId, categoryName);

        return ResponseEntity.ok(response);
    }

    @GetMapping(params = "type") // 'type' 파라미터가 있을 때만 이 메소드가 호출됨
    public ResponseEntity<List<LedgerResponseDto>> getLedgerEntriesByTransactionType(
            @RequestParam("type") TransactionType transactionType
    ) {
        // 토큰에서 현재 사용자 ID 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        // 서비스 호출
        List<LedgerResponseDto> response = findInFoService.findEntriesByTransactionType(userId, transactionType);

        return ResponseEntity.ok(response);
    }
}
