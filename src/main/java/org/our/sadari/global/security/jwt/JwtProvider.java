package org.our.sadari.global.security.jwt;

import io.jsonwebtoken.Claims;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * JWT(JSON Web Token) 생성, 검증, 파싱 및 Spring Security Authentication 객체 생성을 담당하는 컴포넌트 클래스.
 *
 * @author Seunghyeon.Kang
 */
@Component
public class JwtProvider {

    private Key secretKey;

    private final String secret;
    private final long accessTokenValidityMilliSeconds;
    private final long refreshTokenValidityMilliSeconds;

    /**
     * JwtProvider 생성자로, application.yml 설정 파일에서 시크릿 키와 토큰 유효시간(초 단위)을 주입받아 밀리초 단위로 변환한다.
     *
     * @param secret Base64 인코딩된 JWT 비밀키
     * @param accessTokenValiditySeconds Access Token 유효시간(초)
     * @param refreshTokenValiditySeconds Refresh Token 유효시간(초)
     */
    public JwtProvider(
            @Value("${jwt.secret_key}") String secret,
            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValiditySeconds,
            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValiditySeconds) {

        this.secret = secret;
        // yml 설정값은 초 단위이고, JWT exp를 만드는 Date 계산은 millisecond 기준이라 1000을 곱해 변환한다.
        this.accessTokenValidityMilliSeconds = accessTokenValiditySeconds * 1000;
        this.refreshTokenValidityMilliSeconds = refreshTokenValiditySeconds * 1000;
    }

    /**
     * 빈(Bean) 생성 및 의존성 주입 완료 후 실행되는 초기화 메서드로, Base64 시크릿 키를 데코딩하여 SecretKey 객체를 생성한다.
     */
    @PostConstruct
    public void initKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 회원 번호와 권한 정보를 바탕으로 Access Token을 발급한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 회원 번호 (PK)
     * @param role 사용자 권한 (예: USER, ADMIN)
     * @return 생성된 Access Token 문자열
     */
    public String createAccessToken(Long userNumb, String role) {
        Date now = new Date();

        return Jwts.builder()
                .setSubject(String.valueOf(userNumb))
                .setId(UUID.randomUUID().toString())
                .claim("role", role)
                // jti(고유 식별자) 생성을 위한 이중 호출 (마지막에 설정된 UUID가 최종 jti로 등록됨)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenValidityMilliSeconds))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 회원 번호를 바탕으로 Access Token 재발급용 Refresh Token을 발급한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 회원 번호 (PK)
     * @return 생성된 Refresh Token 문자열
     */
    public String createRefreshToken(Long userNumb) {
        Date now = new Date();

        return Jwts.builder()
                .setSubject(String.valueOf(userNumb))
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenValidityMilliSeconds))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 전달받은 JWT 토큰의 서명 위변조 및 만료 여부를 검증한다.
     *
     * @author Seunghyeon.Kang
     * @param token 검증할 JWT 토큰
     * @return 유효성 여부 (true: 유효한 토큰, false: 유효하지 않거나 만료된 토큰)
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 서명 불일치, 만료, 형식 오류 등의 예외 발생 시 검증 실패 처리한다.
            return false;
        }
    }

    /**
     * JWT 토큰에서 회원 번호(sub)를 추출한다.
     *
     * @author Seunghyeon.Kang
     * @param token JWT 토큰
     * @return 추출된 회원 번호 (Long)
     */
    public Long getUserNumb(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    /**
     * JWT 토큰에서 사용자 권한(role)을 추출한다.
     *
     * @author Seunghyeon.Kang
     * @param token JWT 토큰
     * @return 사용자 권한 문자열
     */
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    /**
     * JWT 토큰에서 고유 식별자(jti)를 추출한다.
     *
     * @author Seunghyeon.Kang
     * @param token JWT 토큰
     * @return 토큰 고유 ID (jti)
     */
    public String getTokenId(String token) {
        return getClaims(token).getId();
    }

    /**
     * JWT 토큰의 만료 시간까지 남아있는 시간을 초(second) 단위로 계산하여 반환한다. (블랙리스트 TTL 설정 시 사용)
     *
     * @author Seunghyeon.Kang
     * @param token JWT 토큰
     * @return 남은 유효 시간(초, 최소 0)
     */
    public long getRemainingSeconds(String token) {
        long remainingMillis = getClaims(token).getExpiration().getTime() - System.currentTimeMillis();
        return Math.max(TimeUnit.MILLISECONDS.toSeconds(remainingMillis), 0);
    }

    /**
     * Refresh Token의 설정된 전체 유효기간(초)을 반환한다.
     *
     * @author Seunghyeon.Kang
     * @return Refresh Token 유효기간(초)
     */
    public long getRefreshTokenValiditySeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(refreshTokenValidityMilliSeconds);
    }

    /**
     * JWT 토큰의 서명을 검증하고 클레임(Claims) 정보를 추출한다.
     *
     * @param token JWT 토큰
     * @return 추출된 Claims 객체
     */
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Access Token의 클레임 정보를 바탕으로 Spring Security의 Authentication(인증) 객체를 생성한다.
     *
     * @author Seunghyeon.Kang
     * @param token JWT 토큰
     * @return Spring Security Authentication 객체
     */
    public Authentication getAuthentication(String token) {

        Long userNumb = getUserNumb(token);
        String role = getRole(token);

        // Spring Security 표준 권한 형식("ROLE_")에 맞춰 GrantedAuthority 리스트를 구성한다.
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

        // SecurityContext에 등록할 인증 토큰 객체를 생성한다. (Principal로 userNumb 사용)
        return new UsernamePasswordAuthenticationToken(
                userNumb, // Authentication principal
                null,
                authorities
        );
    }
}
