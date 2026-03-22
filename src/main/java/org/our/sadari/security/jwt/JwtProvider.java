package org.our.sadari.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;

import java.security.Key;
import java.util.Date;
import java.util.List;

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

    private Key secretKey;

    private final String secret;
    private final long accessTokenValidityMilliSeconds;
    private final long refreshTokenValidityMilliSeconds;


    public JwtProvider(
            @Value("${jwt.secret_key}") String secret,
            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValiditySeconds,
            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValiditySeconds) {

        this.secret = secret;
        this.accessTokenValidityMilliSeconds = accessTokenValiditySeconds * 1000;
        this.refreshTokenValidityMilliSeconds = refreshTokenValiditySeconds * 1000;
    }

    /**
     * Bean 생성 이후 한 번 실행되는 초기화 메서드
     * - Base64로 인코딩된 secret 문자열을 디코딩
     * - JWT 서명에 사용할 Key 객체 생성
     * - JWT 서명/검증 시 단순 문자열이 아니라 Key 객체가 필요함
     */
    @PostConstruct
    public void initKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * AccessToken 생성
     * - 사용자의 인증 정보를 담은 JWT 생성
     * - 클라이언트가 API 요청 시 사용하는 토큰
     * @param userNumb 사용자 고유 ID (DB PK)
     * @param role 사용자 권한 (USER, ADMIN 등)
     */
    public String createAccessToken(Long userNumb, String role) {
        Date now = new Date();

        return Jwts.builder()
                .setSubject(String.valueOf(userNumb)) // 🔥 핵심
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenValidityMilliSeconds))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * RefreshToken 생성
     * - AccessToken이 만료되었을 때 재발급을 위한 토큰
     * - DB 및 쿠키에 저장됨
     * - 권한(role)은 필요 없음 (단순 재발급 용도)
     */
    public String createRefreshToken(Long userNumb) {
        Date now = new Date();

        return Jwts.builder()
                .setSubject(String.valueOf(userNumb)) // 🔥 핵심
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenValidityMilliSeconds))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰 유효성 검증
     * - 토큰이 정상적인지 확인
     * - 위조 여부 및 만료 여부 검사
     * - 서명 검증 실패 → Exception 발생
     * - 만료된 토큰 → Exception 발생
     * @return true: 정상 토큰, false: 유효하지 않음
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 토큰에서 사용자 ID 추출
     * JWT의 subject에 저장된 userId 반환
     * @param token JWT 문자열
     * @return 사용자 ID (Long)
     */
    public Long getUserNumb(String token) {
        return Long.parseLong(
                Jwts.parserBuilder()
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject()
        );
    }

    /**
     * 토큰에서 사용자 권한(role) 추출
     * - JWT claim에 저장된 role 값 반환
     * @param token JWT 문자열
     * @return 권한 문자열 (USER, ADMIN 등)
     */
    public String getRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

    /**
     * Authentication 객체 생성
     * JWT 기반 인증을 Spring Security가 이해할 수 있는 형태로 변환
     * Spring Security는 Authentication 객체를 기준으로 "로그인 여부" 판단
     * JWT는 상태가 없기 때문에 매 요청마다 이 객체를 만들어줘야 함
     * - principal: 사용자 식별 정보 (여기서는 userId)
     * - credentials: 인증 정보 (JWT에서는 필요 없으므로 null)
     * - authorities: 권한 목록
     *
     * @param token JWT 문자열
     * @return Authentication 객체
     */
    public Authentication getAuthentication(String token) {

        // 토큰에서 사용자 정보 추출
        Long userNumb = getUserNumb(token);
        String role = getRole(token);

        // Spring Security 권한 객체 생성
        // 반드시 "ROLE_" prefix 필요 (hasRole에서 사용됨)
        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + role));

        // Authentication 구현체 생성
        return new UsernamePasswordAuthenticationToken(
                userNumb, // principal
                null,
                authorities
        );
    }
}