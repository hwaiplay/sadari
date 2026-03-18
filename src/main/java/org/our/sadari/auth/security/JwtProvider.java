package org.our.sadari.auth.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey key =
            Keys.hmacShaKeyFor("mysecretkeymysecretkeymysecretkey".getBytes());

    public String createToken(Long USER_NUMB, String USER_EMIL) {

        return Jwts.builder()
                .setSubject(String.valueOf(USER_NUMB))
                .claim("email", USER_EMIL)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }
}