package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.dto.LoginRequestDto;
import com.knusdp.SmartLedger.dto.SaveUserLoginInfoDto;
import com.knusdp.SmartLedger.entity.User;
import com.knusdp.SmartLedger.repository.UserRepository;
import com.knusdp.SmartLedger.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginRequestDto login(Long userID, String rawPassword){
        Optional<User> userOpt = userRepository.findByUserID(userID);
        if(userOpt.isPresent()){
            User user = userOpt.get();
            System.out.println("DB PW: " + user.getPassword());
            System.out.println("입력 PW: " + rawPassword);
            if(passwordEncoder.matches(rawPassword, user.getPassword())){
                String token = JwtUtil.generateToken(String.valueOf(user.getId()));
                LoginRequestDto userInfo = new LoginRequestDto(
                        user.getId(),
                        user.getEmail(),
                        user.getPassword(),
                        token
                );
                System.out.println("로그인 성공: " + user.getUsername());
                return userInfo;
            }
        }
        return null;
    }
    public User saveUserInfo(SaveUserLoginInfoDto saveUserLoginInfoDto){
        User user = new User();
        user.setUserID(saveUserLoginInfoDto.getUserID());
        user.setUserPassword(passwordEncoder.encode(saveUserLoginInfoDto.getUserPassword()));
        user.setUserName(saveUserLoginInfoDto.getUserName());
        user.setUserYear(saveUserLoginInfoDto.getUserYear());
        user.setSession(saveUserLoginInfoDto.getSession());
        user.setRole("none");
        return userLoginInfoRepository.save(user);
    }

}
