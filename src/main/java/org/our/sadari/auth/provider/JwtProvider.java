package org.our.sadari.auth.provider;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * fileName       : JwtProvider
 * author         : hanWon.Jang
 * date           : 2026-03-18
 * description    : Jwt 토큰 발급
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-18       hanWon.Jang     최초 생성
 */
@Component
public class JwtProvider {

    private final SecretKey key =
            Keys.hmacShaKeyFor("mysecretkeymysecretkeymysecretkey".getBytes());

    public String createToken(Long userNumb, String userIdxx) {

        return Jwts.builder()
                .setSubject(String.valueOf(userNumb))
                .claim("userIdxx", userIdxx)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }
}