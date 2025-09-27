package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.repository.UserRepository;
import com.knusdp.SmartLedger.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
@RequiredArgsConstructor
@Service
public class FindInFoService {
    private final CryptoUtil cryptoUtil;
    private final UserRepository userRepository;

    public Optional<String> findId(String username, String phoneNum, String birth) {
        LocalDate birthDate = LocalDate.parse(birth);
        String encryptedPhone = cryptoUtil.encrypt(phoneNum);
        return userRepository.findByUsernameAndPhoneNumberAndBirth(username, encryptedPhone, birthDate)
                .map(Member::getEmail);
    }

}
