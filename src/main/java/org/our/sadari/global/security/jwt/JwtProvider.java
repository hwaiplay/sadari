package org.our.sadari.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * fileName       : JwtProvider
 * author         : SeungHyeon.Kang
 * date           : 2026-03-22
 * description    : 인증과 보안 외부 연동 기능을 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-22        SeungHyeon.Kang    최초 생성
 */
@Component
public class JwtProvider {
    // JWT 서명 검증용 비밀키
    private SecretKey secretKey;

    // YML에서 읽은 JWT 원본 비밀값
    private final String secret;
    // 액세스 토큰 유효 시간
    private final long accessTokenValidityMilliSeconds;
    // 리프레시 토큰 유효 시간
    private final long refreshTokenValidityMilliSeconds;

    /**
     * JwtProvider 생성자로, application.yml 설정 파일에서 시크릿 키와 토큰 유효시간(초 단위)을 주입받아 밀리초 단위로 변환한다.
     *
     * @param secret Base64 인코딩된 JWT 비밀키
     * @param accessTokenValiditySeconds Access Token 유효시간(초)
     * @param refreshTokenValiditySeconds Refresh Token 유효시간(초)
     */
    public JwtProvider(@Value("${jwt.secret_key}") String secret, @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValiditySeconds
                     , @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValiditySeconds) {

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
        // 인코딩된 값을 원문 형식으로 복원한다
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        // 설정된 비밀키로 JWT HMAC 서명 키를 생성한다
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 회원 번호와 권한 정보를 바탕으로 Access Token을 발급한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호 (PK)
     * @param role 사용자 권한 (예: USER, ADMIN)
     * @return 생성된 Access Token 문자열
     */
    public String createAccessToken(Long userNumb, String role) {
        // JWT 만료 시각을 담을 객체를 생성한다
        Date now = new Date();
        // 회원 번호와 권한 정보를 바탕으로 Access Token을 발급 결과를 반환한다
        return Jwts.builder()
                .subject(String.valueOf(userNumb))
                .id(UUID.randomUUID().toString())
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenValidityMilliSeconds))
                // 0.13 API의 알고리즘 레지스트리를 명시해 서명 알고리즘을 HS256으로 고정한다.
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 회원 번호를 바탕으로 Access Token 재발급용 Refresh Token을 발급한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호 (PK)
     * @return 생성된 Refresh Token 문자열
     */
    public String createRefreshToken(Long userNumb) {
        // JWT 만료 시각을 담을 객체를 생성한다
        Date now = new Date();
        // 회원 번호를 바탕으로 Access Token 재발급용 Refresh Token을 발급 결과를 반환한다
        return Jwts.builder()
                .subject(String.valueOf(userNumb))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenValidityMilliSeconds))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 전달받은 JWT 토큰의 서명 위변조 및 만료 여부를 검증한다.
     *
     * @author SeungHyeon.Kang
     * @param token 검증할 JWT 토큰
     * @return 유효성 여부 (true: 유효한 토큰, false: 유효하지 않거나 만료된 토큰)
     */
    public boolean validateToken(String token) {
        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            /*
             * JJWT 0.13에서는 parserBuilder가 parser로 변경되었다.
             * verifyWith로 검증 키를 고정하고 서명된 Claims만 허용해 unsigned JWT가 통과하지 못하게 한다.
             */
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            // 전달받은 JWT 토큰의 서명 위변조 및 만료 여부를 검증 판정값을 반환한다
            return true;
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception e) {
            // 전달받은 JWT 토큰의 서명 위변조 및 만료 여부를 검증 판정값을 반환한다
            return false;
        }
    }

    /**
     * JWT 토큰에서 회원 번호(sub)를 추출한다.
     *
     * @author SeungHyeon.Kang
     * @param token JWT 토큰
     * @return 추출된 회원 번호 (Long)
     */
    public Long getUserNumb(String token) {
        // JWT 토큰에서 회원 번호(sub)를 추출 결과를 반환한다
        return Long.parseLong(getClaims(token).getSubject());
    }

    /**
     * JWT 토큰에서 사용자 권한(role)을 추출한다.
     *
     * @author SeungHyeon.Kang
     * @param token JWT 토큰
     * @return 사용자 권한 문자열
     */
    public String getRole(String token) {
        // JWT 토큰에서 사용자 권한(role)을 추출 결과를 반환한다
        return getClaims(token).get("role", String.class);
    }

    /**
     * JWT 토큰에서 고유 식별자(jti)를 추출한다.
     *
     * @author SeungHyeon.Kang
     * @param token JWT 토큰
     * @return 토큰 고유 ID (jti)
     */
    public String getTokenId(String token) {
        // JWT 토큰에서 고유 식별자(jti)를 추출 결과를 반환한다
        return getClaims(token).getId();
    }

    /**
     * JWT 토큰의 만료 시간까지 남아있는 시간을 초(second) 단위로 계산하여 반환한다. (블랙리스트 TTL 설정 시 사용)
     *
     * @author SeungHyeon.Kang
     * @param token JWT 토큰
     * @return 남은 유효 시간(초, 최소 0)
     */
    public long getRemainingSeconds(String token) {
        // 만료 시각 계산에 사용할 현재 시각을 조회한다
        long remainingMillis = getClaims(token).getExpiration().getTime() - System.currentTimeMillis();
        // JWT 토큰의 만료 시간까지 남아있는 시간을 초(second) 단위로 계산하여 반환한다. (블랙리스트 TTL 설정 시 사용) 결과를 반환한다
        return Math.max(TimeUnit.MILLISECONDS.toSeconds(remainingMillis), 0);
    }

    /**
     * Refresh Token의 설정된 전체 유효기간(초)을 반환한다.
     *
     * @author SeungHyeon.Kang
     * @return Refresh Token 유효기간(초)
     */
    public long getRefreshTokenValiditySeconds() {
        // Refresh Token의 설정된 전체 유효기간(초)을 반환한다
        return TimeUnit.MILLISECONDS.toSeconds(refreshTokenValidityMilliSeconds);
    }

    /**
     * JWT 토큰의 서명을 검증하고 클레임(Claims) 정보를 추출한다.
     *
     * @param token JWT 토큰
     * @return 추출된 Claims 객체
     */
    private Claims getClaims(String token) {
        // JWT 토큰의 서명을 검증하고 클레임(Claims) 정보를 추출 결과를 반환한다
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Access Token의 클레임 정보를 바탕으로 Spring Security의 Authentication(인증) 객체를 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param token JWT 토큰
     * @return Spring Security Authentication 객체
     */
    public Authentication getAuthentication(String token) {
        // getUserNumb 조회로 후속 처리에 필요한 데이터를 가져온다
        Long userNumb = getUserNumb(token);
        // getRole 조회로 후속 처리에 필요한 데이터를 가져온다
        String role = getRole(token);

        // Spring Security 표준 권한 형식("ROLE_")에 맞춰 GrantedAuthority 리스트를 구성한다.
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

        // 새로 생성한 UsernamePasswordAuthenticationToken 객체를 반환한다
        return new UsernamePasswordAuthenticationToken(userNumb, null, authorities);
    }
}
