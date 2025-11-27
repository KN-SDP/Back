package com.knusdp.SmartLedger.util;

import com.knusdp.SmartLedger.entity.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret-key}")
    private String secretKeyString;
    private SecretKey SECRET_KEY;
    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1시간

    @PostConstruct
    public void init() {
        // 주입받은 Base64 문자열 키를 SecretKey 객체로 변환
        byte[] keyBytes = Base64.getDecoder().decode(secretKeyString);
        this.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
    }
    // JWT 생성
    public String generateToken(Member member) {

        Claims claims = Jwts.claims().setSubject(String.valueOf(member.getId()));
        claims.put("username", member.getUsername());
        claims.put("nickname", member.getNickname());
        claims.put("email", member.getEmail());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT 검증
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 만료 시간 직접 확인
            return !claims.getExpiration().before(new Date());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println("토큰 만료됨");
            return false;
        } catch (JwtException e) {
            System.out.println("JWT 검증 실패");
            return false;
        }
    }

    public String getUserIdFromToken(String token) {

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // JWT에서 사용자 이름 추출
    public String getUsernameFromToken(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("username", String.class);
    }

    // JWT에서 닉네임 추출
    public String getNicknameFromToken(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("nickname", String.class);
    }
    public String getEmailFromToken(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("email", String.class);
    }

    // 비밀번호 재설정을 위한 JWT
    public String generateResetToken(Long userId, String email) {

        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        claims.put("email", email);
        claims.put("type", "RESET_PASSWORD"); // 토큰 종류 구분

        long expiration = 1000 * 60 * 10; // 10분 만료

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }
    // 비밀번호재설정 JWT 검증
    public boolean validateResetToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (!"RESET_PASSWORD".equals(claims.get("type", String.class))) {
                return false;
            }

            return !claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return false;
        }
    }
}