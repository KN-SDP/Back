package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.dto.CreateAccountDto;
import com.knusdp.SmartLedger.dto.LedgerResponseDto;
import com.knusdp.SmartLedger.dto.LedgerSearchRequestDto;
import com.knusdp.SmartLedger.dto.UpdateLedgerRequestDto;
import com.knusdp.SmartLedger.entity.AccountBook;
import com.knusdp.SmartLedger.entity.AccountCategory;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.entity.TransactionType;
import com.knusdp.SmartLedger.exception.InvalidAmountException;
import com.knusdp.SmartLedger.exception.LedgerEntryNotFoundException;
import com.knusdp.SmartLedger.exception.MissingRequiredFieldException;
import com.knusdp.SmartLedger.repository.CategoryRepository;
import com.knusdp.SmartLedger.repository.AccountBookRepository;
import com.knusdp.SmartLedger.repository.MemberRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AccountBookService {
    private final AccountBookRepository accountBookRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    
    //거래내역 생성
    @Transactional
    public void createLedgerEntry(Long memberId, CreateAccountDto dto){
        if (dto.getDate() == null || dto.getDescription() == null ||
                dto.getAmount() == null || dto.getTransactionType() == null ||
                dto.getPaymentType() == null || dto.getCategoryId() == null) {
            throw new MissingRequiredFieldException("필수 입력값이 누락되었습니다.");
        }

        // 금액 0 이하 체크
        if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("금액은 0보다 커야 합니다.");
        }

        // 소수점 입력 체크
        if (dto.getAmount().scale() > 0) {
            throw new InvalidAmountException("금액은 정수만 입력할 수 있습니다.");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        AccountCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));

        AccountBook accountBook = AccountBook.builder()
                .transactionDate(dto.getDate())
                .description(dto.getDescription())
                .amount(dto.getAmount())
                .transactionType(dto.getTransactionType())
                .paymentType(dto.getPaymentType())
                .category(category)
                .member(member)
                .build();

        accountBookRepository.save(accountBook);
    }
    //통합 조회
    @Transactional(readOnly = true)
    public List<LedgerResponseDto> findLedgerEntriesByCriteria(Long memberId, LedgerSearchRequestDto dto) {

        // 1. 기본 조건 (사용자 ID) 설정
        Specification<AccountBook> spec = Specification.where(AccountBookSpecification.hasMemberId(memberId));

        // 2. DTO에 값이 있을 때만 (null이 아닐 때만) 조건을 동적으로 추가
        if (dto.getYear() != null) {
            spec = spec.and(AccountBookSpecification.hasYear(dto.getYear()));
        }
        if (dto.getMonth() != null) {
            if (dto.getMonth() < 1 || dto.getMonth() > 12) {
                throw new IllegalArgumentException("월(month)은 1에서 12 사이의 숫자여야 합니다.");
            }
            spec = spec.and(AccountBookSpecification.hasMonth(dto.getMonth()));
        }
        if (dto.getTransactionType() != null) {
            spec = spec.and(AccountBookSpecification.hasTransactionType(dto.getTransactionType()));
        }
        if (dto.getCategoryName() != null && !dto.getCategoryName().isBlank()) {
            spec = spec.and(AccountBookSpecification.hasCategoryName(dto.getCategoryName()));
        }

        // 3. 조합된 Specification으로 리포지토리 조회
        List<AccountBook> entries = accountBookRepository.findAll(spec);

        // 4. DTO로 변환하여 반환
        return entries.stream()
                .map(LedgerResponseDto::new)
                .collect(Collectors.toList());
    }
    //카테고리 조회
    public List<LedgerResponseDto> findEntriesByCategory(Long memberId, String categoryName) {
        List<AccountBook> entries = accountBookRepository.findByMemberAndCategoryName(memberId, categoryName);

        return entries.stream()
                .map(LedgerResponseDto::new)
                .collect(Collectors.toList());
    }
    //거래타입별 조회
    public List<LedgerResponseDto> findEntriesByTransactionType(Long memberId, TransactionType transactionType) {
        List<AccountBook> entries = accountBookRepository.findByMemberIdAndTransactionType(memberId, transactionType);

        return entries.stream()
                .map(LedgerResponseDto::new)
                .collect(Collectors.toList());
    }
    //년월별 조회
    public List<LedgerResponseDto> findLedgerEntriesByYearAndMonth(Long memberId, int year, int month){
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("월(month)은 1에서 12 사이의 숫자여야 합니다.");
        }

        List<AccountBook> entries = accountBookRepository.findEntriesByYearAndMonth(memberId, year, month);

        return entries.stream()
                .map(LedgerResponseDto::new) // .map(entry -> new LedgerResponseDto(entry))와 동일
                .collect(Collectors.toList());
    }
    //거래내역 상세조회
    public LedgerResponseDto findLedgerEntryById(Long memberId, Long transactionId) {
        AccountBook entry = accountBookRepository.findByMemberIdAndTransactionId(memberId, transactionId)
                .orElseThrow(() -> new LedgerEntryNotFoundException("해당 가계부 내역을 찾을 수 없습니다."));

        return new LedgerResponseDto(entry);
    }
    //거래내역 수정
    @Transactional // 데이터를 변경하므로 @Transactional이 필수입니다.
    public LedgerResponseDto updateLedgerEntry(Long memberId, Long transactionId, UpdateLedgerRequestDto dto) {
        AccountBook entryToUpdate = accountBookRepository.findByMemberIdAndTransactionId(memberId, transactionId)
                .orElseThrow(() -> new LedgerEntryNotFoundException("해당 가계부 내역을 찾을 수 없습니다."));

        if (dto.getDate() != null) {
            entryToUpdate.setTransactionDate(dto.getDate());
        }
        if (dto.getDescription() != null) {
            entryToUpdate.setDescription(dto.getDescription());
        }
        if (dto.getAmount() != null) {
            if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidAmountException("금액은 0보다 커야 합니다.");
            }
            entryToUpdate.setAmount(dto.getAmount());
        }
        if (dto.getTransactionType() != null) {
            entryToUpdate.setTransactionType(dto.getTransactionType());
        }
        if (dto.getPaymentType() != null) {
            entryToUpdate.setPaymentType(dto.getPaymentType());
        }
        if (dto.getCategoryId() != null) {
            AccountCategory newCategory = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
            entryToUpdate.setCategory(newCategory);
        }
        return new LedgerResponseDto(entryToUpdate);
    }
    //거래내역 삭제
    @Transactional
    public void deleteLedgerEntry(Long memberId, Long transactionId) {
        AccountBook entryToDelete = accountBookRepository.findByMemberIdAndTransactionId(memberId, transactionId)
                .orElseThrow(() -> new LedgerEntryNotFoundException("해당 가계부 내역을 찾을 수 없습니다."));

        accountBookRepository.delete(entryToDelete);
    }

}
