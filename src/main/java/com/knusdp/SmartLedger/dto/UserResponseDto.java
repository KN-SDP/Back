package com.knusdp.SmartLedger.dto;
import com.knusdp.SmartLedger.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String email;
    private String username;
    private String nickname;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.nickname = user.getNickname();
    }
}
