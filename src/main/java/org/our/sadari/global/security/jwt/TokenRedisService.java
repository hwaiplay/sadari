package org.our.sadari.global.security.jwt;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * JWT logout과 refresh token rotation에 필요한 Redis 저장소 접근을 담당합니다.
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
     * 사용자별 refreshToken을 Redis에 저장합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb refreshToken 소유 회원 번호
     * @param refreshToken 저장할 refreshToken
     * @param ttlSeconds Redis에 유지할 초 단위 만료 시간
     */
    public void setRefreshToken(Long userNumb, String refreshToken, long ttlSeconds) {
        redisTemplate.opsForValue().set(
                getRefreshTokenKey(userNumb),
                refreshToken,
                Duration.ofSeconds(ttlSeconds)
        );
    }

    /**
     * 사용자별 refreshToken을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb refreshToken 소유 회원 번호
     * @return Redis에 저장된 refreshToken
     */
    public String getRefreshToken(Long userNumb) {
        return redisTemplate.opsForValue().get(getRefreshTokenKey(userNumb));
    }

    /**
     * 사용자별 refreshToken을 삭제해 재발급 권한을 차단합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb refreshToken을 삭제할 회원 번호
     */
    public void deleteRefreshToken(Long userNumb) {
        redisTemplate.delete(getRefreshTokenKey(userNumb));
    }

    /**
     * logout된 accessToken의 JWT ID를 Redis blacklist에 저장합니다.
     *
     * @author Seunghyeon.Kang
     * @param tokenId accessToken의 JWT ID
     * @param ttlSeconds accessToken 잔여 만료 시간
     */
    public void setAccessTokenBlacklist(String tokenId, long ttlSeconds) {
        // JWT ID가 없거나 이미 만료된 토큰은 차단 목록에 보관할 필요가 없습니다.
        if (StringUtil.isEmpty(tokenId) || ttlSeconds <= 0) {
            return;
        }

        redisTemplate.opsForValue().set(
                getAccessTokenBlacklistKey(tokenId),
                "logout",
                Duration.ofSeconds(ttlSeconds)
        );
    }

    /**
     * accessToken의 JWT ID가 logout blacklist에 존재하는지 확인합니다.
     *
     * @author Seunghyeon.Kang
     * @param tokenId accessToken의 JWT ID
     * @return blacklist에 있으면 true, 없으면 false
     */
    public boolean hasAccessTokenBlacklist(String tokenId) {
        // 토큰 ID가 없으면 blacklist 조회 없이 인증 실패 흐름에 맡깁니다.
        if (StringUtil.isEmpty(tokenId)) {
            return false;
        }

        return Boolean.TRUE.equals(redisTemplate.hasKey(getAccessTokenBlacklistKey(tokenId)));
    }

    /**
     * 사용자 번호 기반 refreshToken Redis key를 생성합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 회원 번호
     * @return refreshToken Redis key
     */
    private String getRefreshTokenKey(Long userNumb) {
        return REFRESH_TOKEN_PREFIX + userNumb;
    }

    /**
     * JWT ID 기반 accessToken blacklist Redis key를 생성합니다.
     *
     * @author Seunghyeon.Kang
     * @param tokenId accessToken의 JWT ID
     * @return accessToken blacklist Redis key
     */
    private String getAccessTokenBlacklistKey(String tokenId) {
        return ACCESS_TOKEN_BLACKLIST_PREFIX + tokenId;
    }
}
