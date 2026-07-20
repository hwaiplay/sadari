package org.our.sadari.global.security.jwt;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * JWT 토큰 관련 Redis 데이터 처리 서비스 클래스.
 * Refresh Token 보관 및 Access Token 블랙리스트 관리를 담당한다.
 *
 * @author Seunghyeon.Kang
 */
@Service
@RequiredArgsConstructor
public class TokenRedisService {

    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";
    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "auth:blacklist:access:";

    private final StringRedisTemplate redisTemplate;

    /**
     * 회원 번호 기준 Refresh Token을 Redis에 저장하고 유효 기간(TTL)을 설정한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 회원 번호 (PK)
     * @param refreshToken 저장할 Refresh Token
     * @param ttlSeconds 토큰 유효 시간(초)
     */
    public void setRefreshToken(Long userNumb, String refreshToken, Long ttlSeconds) {
        redisTemplate.opsForValue().set(getRefreshTokenKey(userNumb), refreshToken, Duration.ofSeconds(ttlSeconds));
    }

    /**
     * 회원 번호로 Redis에 저장된 Refresh Token을 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 회원 번호 (PK)
     * @return 저장된 Refresh Token (없을 경우 null)
     */
    public String getRefreshToken(Long userNumb) {
        return redisTemplate.opsForValue().get(getRefreshTokenKey(userNumb));
    }

    /**
     * 회원 번호 기준 Redis에 저장된 Refresh Token을 삭제한다. (로그아웃/만료 시 호출)
     *
     * @author Seunghyeon.Kang
     * @param userNumb 회원 번호 (PK)
     */
    public void deleteRefreshToken(Long userNumb) {
        redisTemplate.delete(getRefreshTokenKey(userNumb));
    }

    /**
     * 로그아웃 처리된 Access Token의 식별자(jti)를 Redis 블랙리스트에 등록한다.
     *
     * @author Seunghyeon.Kang
     * @param tokenId Access Token의 고유 식별자 (jti)
     * @param ttlSeconds Access Token의 남은 유효 시간(초)
     */
    public void setAccessTokenBlacklist(String tokenId, long ttlSeconds) {
        // 토큰 식별자가 없거나 만료 시간이 유효하지 않은(0 이하) 경우 블랙리스트에 등록하지 않고 종료한다.
        if (StringUtil.isEmpty(tokenId) || ttlSeconds <= 0) {
            return;
        }

        redisTemplate.opsForValue().set(getAccessTokenBlacklistKey(tokenId), "logout", Duration.ofSeconds(ttlSeconds));
    }

    /**
     * 전달받은 Access Token 식별자(jti)가 Redis 블랙리스트에 존재하는지 검증한다.
     *
     * @author Seunghyeon.Kang
     * @param tokenId Access Token의 고유 식별자 (jti)
     * @return 블랙리스트 등록 여부 (true: 로그아웃된 토큰, false: 사용 가능한 토큰)
     */
    public boolean hasAccessTokenBlacklist(String tokenId) {
        // 토큰 식별자(jti)가 전달되지 않은 경우 정상적인 조회 불가로 판단하여 false를 반환한다.
        if (StringUtil.isEmpty(tokenId)) {
            return false;
        }

        return Boolean.TRUE.equals(redisTemplate.hasKey(getAccessTokenBlacklistKey(tokenId)));
    }

    /**
     * Refresh Token 저장용 Redis Key를 생성한다. (형식: auth:refresh:{userNumb})
     *
     * @author Seunghyeon.Kang
     * @param userNumb 회원 번호 (PK)
     * @return Redis Key 문자열
     */
    private String getRefreshTokenKey(Long userNumb) {
        return REFRESH_TOKEN_PREFIX + userNumb;
    }

    /**
     * Access Token 블랙리스트 저장용 Redis Key를 생성한다. (형식: auth:blacklist:access:{tokenId})
     *
     * @author Seunghyeon.Kang
     * @param tokenId Access Token 고유 식별자 (jti)
     * @return Redis Key 문자열
     */
    private String getAccessTokenBlacklistKey(String tokenId) {
        return ACCESS_TOKEN_BLACKLIST_PREFIX + tokenId;
    }
}