package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.exception.UserNotFoundException;
import com.knusdp.SmartLedger.repository.AccountBookRepository;
import com.knusdp.SmartLedger.repository.CategoryRepository;
import com.knusdp.SmartLedger.repository.MemberRepository;
import com.knusdp.SmartLedger.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class FindInFoService {
    private final CryptoUtil cryptoUtil;
    private final MemberRepository memberRepository;
    private final AccountBookRepository accountBookRepository;
    private final CategoryRepository categoryRepository;

    public String findId(String username, String phoneNum, String birth) {
        LocalDate birthDate = LocalDate.parse(birth);
        String encryptedPhone = cryptoUtil.encrypt(phoneNum);

        return memberRepository.findByUsernameAndPhoneNumberAndBirth(username, encryptedPhone, birthDate)
                .map(Member::getEmail)
                .orElseThrow(() -> new UserNotFoundException("일치하는 계정을 찾을 수 없습니다."));
    }
}
