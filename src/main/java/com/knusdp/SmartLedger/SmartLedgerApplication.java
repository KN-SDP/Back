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
    public static void main(String[] args) {
        SpringApplication.run(SmartLedgerApplication.class, args);
    }

}
