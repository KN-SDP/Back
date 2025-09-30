package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.dto.CreateAccountDto;
import com.knusdp.SmartLedger.entity.*;
import com.knusdp.SmartLedger.exception.InvalidAmountException;
import com.knusdp.SmartLedger.repository.CategoryRepository;
import com.knusdp.SmartLedger.repository.AccountBookRepository;
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
class AccountBookServiceTest { // 테스트 클래스 이름은 Service를 테스트하므로 LedgerServiceTest가 더 적절합니다.

    @Autowired
    private AccountBookService accountBookService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AccountBookRepository accountBookRepository;

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

    @Test
    @DisplayName("가계부 내역 추가 성공")
    void createLedgerEntry_success() {
        // given
        CreateAccountDto dto = new CreateAccountDto(
                LocalDate.of(2025, 9, 27),
                "점심 식사",
                new BigDecimal("9000"),
                TransactionType.EXPENSE,
                PaymentType.CREDIT_CARD,
                testCategory.getCategoryId()
        );

        // when
        // 반환값이 없으므로 변수에 할당하지 않습니다.
        accountBookService.createLedgerEntry(testUser.getId(), dto);

        // then
        // DB에서 직접 데이터를 조회하여 검증합니다.
        List<AccountBook> entries = accountBookRepository.findAll();
        assertThat(entries).hasSize(1); // 데이터가 1개 저장되었는지 확인

        AccountBook savedEntry = entries.get(0); // 저장된 첫 번째 데이터
        assertThat(savedEntry.getDescription()).isEqualTo("점심 식사");
        assertThat(savedEntry.getAmount()).isEqualTo(new BigDecimal("9000")); //
        assertThat(savedEntry.getMember().getId()).isEqualTo(testUser.getId());
        assertThat(savedEntry.getCategory().getCategoryId()).isEqualTo(testCategory.getCategoryId());
    }

    @Test
    @DisplayName("가계부 내역 추가 실패 - 금액이 소수점일 경우")
    void createLedgerEntry_fail_decimalAmount() {
        // given
        CreateAccountDto dto = new CreateAccountDto(
                LocalDate.now(),
                "소수점 입력",
                new BigDecimal("9000.50"),
                TransactionType.EXPENSE,
                PaymentType.CASH,
                testCategory.getCategoryId()
        );

        // when & then
        assertThrows(InvalidAmountException.class, () -> {
            accountBookService.createLedgerEntry(testUser.getId(), dto);
        });
    }


    @Test
    @DisplayName("가계부 내역 추가 실패 - 금액이 0 이하일 경우")
    void createLedgerEntry_fail_invalidAmount() {
        // given
        CreateAccountDto dto = new CreateAccountDto(
                LocalDate.now(), "Test", BigDecimal.ZERO, TransactionType.EXPENSE, PaymentType.CASH, testCategory.getCategoryId());

        // when & then
        // 예외 발생 여부만 확인하면 되므로 이 테스트는 수정할 필요가 없습니다.
        assertThrows(InvalidAmountException.class, () -> {
            accountBookService.createLedgerEntry(testUser.getId(), dto);
        });
    }

    @Test
    @DisplayName("가계부 내역 추가 실패 - 존재하지 않는 카테고리 ID")
    void createLedgerEntry_fail_categoryNotFound() {
        // given
        Long nonExistentCategoryId = 9999L;
        CreateAccountDto dto = new CreateAccountDto(
                LocalDate.now(), "Test", new BigDecimal("5000"), TransactionType.EXPENSE, PaymentType.CASH, nonExistentCategoryId);

        // when & then
        // 예외 발생 여부만 확인하면 되므로 이 테스트는 수정할 필요가 없습니다.
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountBookService.createLedgerEntry(testUser.getId(), dto);
        });
        assertThat(exception.getMessage()).isEqualTo("카테고리를 찾을 수 없습니다.");
    }
}