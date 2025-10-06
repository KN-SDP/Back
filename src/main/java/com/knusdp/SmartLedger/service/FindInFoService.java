package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.dto.LedgerResponseDto;
import com.knusdp.SmartLedger.entity.AccountBook;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.entity.TransactionType;
import com.knusdp.SmartLedger.exception.UserNotFoundException;
import com.knusdp.SmartLedger.repository.AccountBookRepository;
import com.knusdp.SmartLedger.repository.MemberRepository;
import com.knusdp.SmartLedger.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FindInFoService {
    private final CryptoUtil cryptoUtil;
    private final MemberRepository memberRepository;
    private final AccountBookRepository accountBookRepository;

    public String findId(String username, String phoneNum, String birth) {
        LocalDate birthDate = LocalDate.parse(birth);
        String encryptedPhone = cryptoUtil.encrypt(phoneNum);

        return memberRepository.findByUsernameAndPhoneNumberAndBirth(username, encryptedPhone, birthDate)
                .map(Member::getEmail)
                .orElseThrow(() -> new UserNotFoundException("일치하는 계정을 찾을 수 없습니다."));
    }
    public List<LedgerResponseDto> findEntriesByCategory(Long memberId, String categoryName) {
        List<AccountBook> entries = accountBookRepository.findByMemberAndCategoryName(memberId, categoryName);

        return entries.stream()
                .map(LedgerResponseDto::new)
                .collect(Collectors.toList());
    }
    public List<LedgerResponseDto> findEntriesByTransactionType(Long memberId, TransactionType transactionType) {
        List<AccountBook> entries = accountBookRepository.findByMemberIdAndTransactionType(memberId, transactionType);

        return entries.stream()
                .map(LedgerResponseDto::new)
                .collect(Collectors.toList());
    }
}
