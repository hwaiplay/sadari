package org.our.sadari.global.security.jwt;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * TokenRedisService 클래스의 역할과 책임을 정의한다.
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
     * setRefreshToken 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 처리에 필요한 입력값
     * @param refreshToken 처리에 필요한 입력값
     * @param ttlSeconds 처리에 필요한 입력값
     */
    public void setRefreshToken(
            Long userNumb,
            String refreshToken,
            long ttlSeconds
    ) {
        redisTemplate.opsForValue().set(
                getRefreshTokenKey(userNumb),
                refreshToken,
                Duration.ofSeconds(ttlSeconds)
        );
    }

    /**
     * getRefreshToken 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 처리에 필요한 입력값
     * @return 처리 결과
     */
    public String getRefreshToken(Long userNumb) {
        return redisTemplate.opsForValue().get(getRefreshTokenKey(userNumb));
    }

    /**
     * deleteRefreshToken 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 처리에 필요한 입력값
     */
    public void deleteRefreshToken(Long userNumb) {
        redisTemplate.delete(getRefreshTokenKey(userNumb));
    }

    /**
     * setAccessTokenBlacklist 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param tokenId 처리에 필요한 입력값
     * @param ttlSeconds 처리에 필요한 입력값
     */
    public void setAccessTokenBlacklist(
            String tokenId,
            long ttlSeconds
    ) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
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
     * hasAccessTokenBlacklist 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param tokenId 처리에 필요한 입력값
     * @return 처리 결과
     */
    public boolean hasAccessTokenBlacklist(String tokenId) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (StringUtil.isEmpty(tokenId)) {
            return false;
        }

        return Boolean.TRUE.equals(redisTemplate.hasKey(getAccessTokenBlacklistKey(tokenId)));
    }

    /**
     * getRefreshTokenKey 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 처리에 필요한 입력값
     * @return 처리 결과
     */
    private String getRefreshTokenKey(Long userNumb) {
        return REFRESH_TOKEN_PREFIX + userNumb;
    }

    /**
     * getAccessTokenBlacklistKey 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param tokenId 처리에 필요한 입력값
     * @return 처리 결과
     */
    private String getAccessTokenBlacklistKey(String tokenId) {
        return ACCESS_TOKEN_BLACKLIST_PREFIX + tokenId;
    }
}
