package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.dto.CreateAccountDto;
import com.knusdp.SmartLedger.dto.UpdateLedgerRequestDto;
import com.knusdp.SmartLedger.entity.*;
import com.knusdp.SmartLedger.exception.InvalidAmountException;
import com.knusdp.SmartLedger.exception.LedgerEntryNotFoundException;
import com.knusdp.SmartLedger.repository.AccountBookRepository;
import com.knusdp.SmartLedger.repository.CategoryRepository;
import com.knusdp.SmartLedger.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class AccountBookServiceTest {

    @Autowired private AccountBookService accountBookService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private AccountBookRepository accountBookRepository;

    private Member testUser;
    private AccountCategory testCategory;

    @BeforeEach
    void setup() {
        testUser = memberRepository.save(Member.builder()
                .email("test@user.com")
                .password("1234")
                .username("테스트유저")
                .nickname("테스터")
                .phoneNumber("encrypted_phone")
                .birth(LocalDate.now())
                .build());

        testCategory = categoryRepository.save(AccountCategory.builder()
                .categoryName("식비")
                .member(testUser)
                .build());
    }

    //  기존 테스트 유지
    @Test
    @DisplayName("가계부 내역 추가 성공")
    void createLedgerEntry_success() {
        CreateAccountDto dto = new CreateAccountDto(
                LocalDate.of(2025, 9, 27),
                "점심 식사",
                new BigDecimal("9000"),
                TransactionType.EXPENSE,
                PaymentType.CREDIT_CARD,
                testCategory.getCategoryId()
        );

        accountBookService.createLedgerEntry(testUser.getId(), dto);

        List<AccountBook> entries = accountBookRepository.findAll();
        assertThat(entries).hasSize(1);

        AccountBook savedEntry = entries.get(0);
        assertThat(savedEntry.getDescription()).isEqualTo("점심 식사");
        assertThat(savedEntry.getAmount()).isEqualByComparingTo("9000");
        assertThat(savedEntry.getMember().getId()).isEqualTo(testUser.getId());
        assertThat(savedEntry.getCategory().getCategoryId()).isEqualTo(testCategory.getCategoryId());
    }

    @Test
    @DisplayName("가계부 내역 추가 실패 - 금액이 소수점일 경우")
    void createLedgerEntry_fail_decimalAmount() {
        CreateAccountDto dto = new CreateAccountDto(
                LocalDate.now(),
                "소수점 입력",
                new BigDecimal("9000.50"),
                TransactionType.EXPENSE,
                PaymentType.CASH,
                testCategory.getCategoryId()
        );

        assertThrows(InvalidAmountException.class, () -> accountBookService.createLedgerEntry(testUser.getId(), dto));
    }

    @Test
    @DisplayName("가계부 내역 추가 실패 - 금액이 0 이하일 경우")
    void createLedgerEntry_fail_invalidAmount() {
        CreateAccountDto dto = new CreateAccountDto(
                LocalDate.now(), "Test", BigDecimal.ZERO, TransactionType.EXPENSE, PaymentType.CASH, testCategory.getCategoryId());

        assertThrows(InvalidAmountException.class, () -> accountBookService.createLedgerEntry(testUser.getId(), dto));
    }

    @Test
    @DisplayName("가계부 내역 추가 실패 - 존재하지 않는 카테고리 ID")
    void createLedgerEntry_fail_categoryNotFound() {
        Long nonExistentCategoryId = 9999L;
        CreateAccountDto dto = new CreateAccountDto(
                LocalDate.now(), "Test", new BigDecimal("5000"), TransactionType.EXPENSE, PaymentType.CASH, nonExistentCategoryId);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> accountBookService.createLedgerEntry(testUser.getId(), dto));
        assertThat(exception.getMessage()).isEqualTo("카테고리를 찾을 수 없습니다.");
    }

    //  추가 테스트 1 — 단일 내역 조회
    @Test
    @DisplayName("가계부 단일 내역 조회 성공")
    void findLedgerEntryById_success() {
        // given
        AccountBook entry = accountBookRepository.save(AccountBook.builder()
                .transactionDate(LocalDate.now())
                .description("커피")
                .amount(new BigDecimal("4500"))
                .transactionType(TransactionType.EXPENSE)
                .paymentType(PaymentType.CASH)
                .category(testCategory)
                .member(testUser)
                .build());

        // when
        var result = accountBookService.findLedgerEntryById(testUser.getId(), entry.getTransactionId());

        // then
        assertThat(result.getDescription()).isEqualTo("커피");
        assertThat(result.getAmount()).isEqualByComparingTo("4500");
    }

    //  추가 테스트 2 — 내역 수정 성공
    @Test
    @DisplayName("가계부 내역 수정 성공")
    void updateLedgerEntry_success() {
        // given
        AccountBook entry = accountBookRepository.save(AccountBook.builder()
                .transactionDate(LocalDate.now())
                .description("커피")
                .amount(new BigDecimal("4500"))
                .transactionType(TransactionType.EXPENSE)
                .paymentType(PaymentType.CASH)
                .category(testCategory)
                .member(testUser)
                .build());

        UpdateLedgerRequestDto dto = new UpdateLedgerRequestDto();
        dto.setDescription("수정된 커피");
        dto.setAmount(new BigDecimal("5000"));

        // when
        var updated = accountBookService.updateLedgerEntry(testUser.getId(), entry.getTransactionId(), dto);

        // then
        assertThat(updated.getDescription()).isEqualTo("수정된 커피");
        assertThat(updated.getAmount()).isEqualByComparingTo("5000");
    }

    //  추가 테스트 3 — 삭제 성공
    @Test
    @DisplayName("가계부 내역 삭제 성공")
    void deleteLedgerEntry_success() {
        // given
        AccountBook entry = accountBookRepository.save(AccountBook.builder()
                .transactionDate(LocalDate.now())
                .description("지우기 테스트")
                .amount(new BigDecimal("7000"))
                .transactionType(TransactionType.EXPENSE)
                .paymentType(PaymentType.CASH)
                .category(testCategory)
                .member(testUser)
                .build());

        // when
        accountBookService.deleteLedgerEntry(testUser.getId(), entry.getTransactionId());

        // then
        List<AccountBook> remaining = accountBookRepository.findAll();
        assertThat(remaining).isEmpty();
    }

    // 추가 테스트 4 — 존재하지 않는 내역 조회 시 예외
    @Test
    @DisplayName("존재하지 않는 내역 조회 시 예외 발생")
    void findLedgerEntryById_fail_notFound() {
        assertThrows(LedgerEntryNotFoundException.class,
                () -> accountBookService.findLedgerEntryById(testUser.getId(), 9999L));
    }

    // 추가 테스트 5 — 특정 연도·월로 조회 성공
    @Test
    @DisplayName("특정 연도와 월로 가계부 내역 조회 성공")
    void findLedgerEntriesByYearAndMonth_success() {
        // given
        accountBookRepository.save(AccountBook.builder()
                .transactionDate(LocalDate.of(2025, 10, 10))
                .description("점심")
                .amount(new BigDecimal("8000"))
                .transactionType(TransactionType.EXPENSE)
                .paymentType(PaymentType.CASH)
                .category(testCategory)
                .member(testUser)
                .build());

        // when
        var result = accountBookService.findLedgerEntriesByYearAndMonth(testUser.getId(), 2025, 10);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("점심");
    }
}
