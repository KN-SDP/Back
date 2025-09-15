package com.knusdp.SmartLedger;

import com.knusdp.SmartLedger.entity.User;
import com.knusdp.SmartLedger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@RequiredArgsConstructor
public class SmartLedgerApplication {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(SmartLedgerApplication.class, args);
    }

    // 애플리케이션 시작될 때 실행
    @Bean
    public CommandLineRunner testSaveUser() {
        return args -> {
            User user = User.builder()
                    .username("jiwoo")
                    .email("21312321@test.com")
                    .password(passwordEncoder.encode("123456"))
                    .phoneNumber("01012341234")
                    .birth("20000101")
                    .build();

            userRepository.save(user); //DB에 저장
            System.out.println("저장된 유저 ID: " + user.getId());
        };
    }
}
