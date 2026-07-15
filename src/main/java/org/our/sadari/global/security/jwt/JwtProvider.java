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

    @PostConstruct
    public void initKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(Long userNumb, String role) {
        Date now = new Date();

        return Jwts.builder()
                .setSubject(String.valueOf(userNumb))
                .setId(UUID.randomUUID().toString())
                .claim("role", role)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenValidityMilliSeconds))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Long userNumb) {
        Date now = new Date();

        return Jwts.builder()
                .setSubject(String.valueOf(userNumb))
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenValidityMilliSeconds))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

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

    public Long getUserNumb(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String getTokenId(String token) {
        return getClaims(token).getId();
    }

    public long getRemainingSeconds(String token) {
        long remainingMillis = getClaims(token).getExpiration().getTime() - System.currentTimeMillis();
        return Math.max(TimeUnit.MILLISECONDS.toSeconds(remainingMillis), 0);
    }

    public long getRefreshTokenValiditySeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(refreshTokenValidityMilliSeconds);
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Authentication getAuthentication(String token) {

        Long userNumb = getUserNumb(token);
        String role = getRole(token);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

        return new UsernamePasswordAuthenticationToken(
                userNumb, // principal
                null,
                authorities
        );
    }
}
