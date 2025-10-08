package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.CreateAccountDto;
import com.knusdp.SmartLedger.dto.LedgerResponseDto;
import com.knusdp.SmartLedger.dto.UpdateLedgerRequestDto;
import com.knusdp.SmartLedger.entity.TransactionType;
import com.knusdp.SmartLedger.service.AccountBookService;
import com.knusdp.SmartLedger.service.FindInFoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
        List<LedgerResponseDto> response = accountBookService.findEntriesByCategory(userId, categoryName);

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
        List<LedgerResponseDto> response = accountBookService.findEntriesByTransactionType(userId, transactionType);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<LedgerResponseDto>> getLedgerEntriesByYearAndMonth(
            @RequestParam("year") int year,
            @RequestParam("month") int month
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        List<LedgerResponseDto> response = accountBookService.findLedgerEntriesByYearAndMonth(userId, year, month);

        // 조회 결과를 200 OK 상태와 함께 반환
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{id}")
    public ResponseEntity<LedgerResponseDto> getLedgerEntry(@PathVariable("id") Long transactionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        LedgerResponseDto response = accountBookService.findLedgerEntryById(userId, transactionId);

        return ResponseEntity.ok(response);
    }
    @PatchMapping("/{id}")
    public ResponseEntity<LedgerResponseDto> updateLedgerEntry(
            @PathVariable("id") Long transactionId,
            @RequestBody UpdateLedgerRequestDto dto
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        LedgerResponseDto response = accountBookService.updateLedgerEntry(userId, transactionId, dto);

        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteLedgerEntry(@PathVariable("id") Long transactionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        accountBookService.deleteLedgerEntry(userId, transactionId);

        Map<String, Object> response = Map.of(
                "status_code", HttpStatus.OK.value(),
                "message", "삭제되었습니다."
        );

        return ResponseEntity.ok(response);
    }
}
