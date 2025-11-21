package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.CreateAccountDto;
import com.knusdp.SmartLedger.dto.LedgerResponseDto;
import com.knusdp.SmartLedger.dto.LedgerSearchRequestDto;
import com.knusdp.SmartLedger.dto.UpdateLedgerRequestDto;
import com.knusdp.SmartLedger.entity.TransactionType;
import com.knusdp.SmartLedger.service.AccountBookService;
import com.knusdp.SmartLedger.service.FindInFoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "bearerAuth")
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
    //통합 조회 api
    @GetMapping
    public ResponseEntity<List<LedgerResponseDto>> searchLedgerEntries(
            // @RequestParam(required = false)를 사용하여 모든 파라미터를 선택적으로 받음
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) TransactionType type
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        // 1. 파라미터를 DTO에 담기
        LedgerSearchRequestDto searchDto = new LedgerSearchRequestDto();
        searchDto.setYear(year);
        searchDto.setMonth(month);
        searchDto.setDay(day);
        searchDto.setCategoryName(category);
        searchDto.setTransactionType(type);

        // 2. 서비스 호출
        List<LedgerResponseDto> response = accountBookService.findLedgerEntriesByCriteria(userId, searchDto);

        return ResponseEntity.ok(response);
    }
    // 카테고리 + 거래유형별 조회 api
    @Transactional(readOnly = true)
    @GetMapping(params = {"category", "type"})
    public ResponseEntity<List<LedgerResponseDto>> getLedgerEntriesByCategoryAndType(
            @RequestParam("category") String categoryName,
            @RequestParam("type") TransactionType transactionType
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        // 서비스 호출 (transactionType 포함)
        List<LedgerResponseDto> response =
                accountBookService.findEntriesByCategory(userId, transactionType, categoryName);

        return ResponseEntity.ok(response);
    }


    //거래타입별 조회 api
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

    //거래내역 상세 조회 api
    @GetMapping("/{id}")
    public ResponseEntity<LedgerResponseDto> getLedgerEntry(@PathVariable("id") Long transactionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        LedgerResponseDto response = accountBookService.findLedgerEntryById(userId, transactionId);

        return ResponseEntity.ok(response);
    }
    //거래내역 업데이트 api
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
    //거래내역삭제 api
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
