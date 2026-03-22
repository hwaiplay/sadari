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

    /*private Key secretkey;

    private final String secretKey;
    private final long accessTokenValidityMilliSeconds;
    private final long refreshTokenValidityMilliSeconds;


    public JwtProvider(@Value("${jwt.secret_key}") String secretKey,
                         @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValiditySeconds,
                         @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValiditySeconds) {
        this.secretKey = secretKey;
        this.accessTokenValidityMilliSeconds = accessTokenValiditySeconds * 1000;
        this.refreshTokenValidityMilliSeconds = refreshTokenValiditySeconds * 1000;
    }

    @PostConstruct
    public void initKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.secretkey = Keys.hmacShaKeyFor(keyBytes);
    }*/

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