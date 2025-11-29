package com.knusdp.SmartLedger.scheduler;

import com.knusdp.SmartLedger.entity.LoginType;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberCleanupSchedulerTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberCleanupScheduler scheduler; // 스케줄러 Bean

    @Test
    @DisplayName("14일 지난 soft-delete 사용자 자동 삭제 테스트")
    void softDeletedMembersShouldBeRemoved() {
        // given
        Member expiredUser = Member.builder()
                .username("jiwoo")
                .email("oldUser@test.com")
                .password("1234")
                .nickname("oldNick")
                .birth(LocalDate.of(1999,1,1))
                .phoneNumber("encrypted_01011111122")
                .loginType(LoginType.LOCAL)
                .deleted(true)
                .deletedAt(LocalDateTime.now().minusDays(15))
                .build();

        Member recentDeleted = Member.builder()
                .username("jiwoo")
                .email("recent@test.com")
                .password("1234")
                .nickname("recentNick")
                .loginType(LoginType.LOCAL)
                .birth(LocalDate.of(1999,1,1))
                .phoneNumber("encrypted_01011212222")
                .deleted(true)
                .deletedAt(LocalDateTime.now().minusDays(5)) // 아직 14일 미만
                .build();

        Member activeUser = Member.builder()
                .username("jiwoo")
                .email("active@test.com")
                .password("1234")
                .birth(LocalDate.of(1999,1,1))
                .phoneNumber("encrypted_01012212222")
                .nickname("activeNick")
                .loginType(LoginType.LOCAL)
                .deleted(false)
                .build();

        memberRepository.save(expiredUser);
        memberRepository.save(recentDeleted);
        memberRepository.save(activeUser);

        // when
        scheduler.cleanupDeletedMembers(); // scheduler 메소드 직접 호출

        // then
        assertThat(memberRepository.findByEmail("oldUser@test.com")).isEmpty();
        assertThat(memberRepository.findByEmail("recent@test.com")).isPresent();
        assertThat(memberRepository.findByEmail("active@test.com")).isPresent();
    }
}