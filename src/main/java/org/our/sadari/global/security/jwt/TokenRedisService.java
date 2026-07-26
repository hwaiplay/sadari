package org.our.sadari.global.security.jwt;

import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
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
    private static final String USER_NICK_PREFIX = "auth:user:nick:";
    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "auth:blacklist:access:";

    private static final DefaultRedisScript<Long> SET_LOGIN_USER_SCRIPT = new DefaultRedisScript<>(
            """
            redis.call('SET', KEYS[1], ARGV[1], 'EX', ARGV[3])
            if ARGV[2] == '' then
                redis.call('DEL', KEYS[2])
            else
                redis.call('SET', KEYS[2], ARGV[2], 'EX', ARGV[3])
            end
            return 1
            """
          , Long.class
    );

    private static final DefaultRedisScript<Long> UPDATE_USER_NICK_SCRIPT = new DefaultRedisScript<>(
            """
            local refreshTtl = redis.call('TTL', KEYS[1])
            if refreshTtl <= 0 then
                redis.call('DEL', KEYS[2])
                return 0
            end
            redis.call('SET', KEYS[2], ARGV[1], 'EX', refreshTtl)
            return 1
            """
          , Long.class
    );

    private final StringRedisTemplate redisTemplate;

    /**
     * 로그인 사용자의 Refresh Token과 닉네임을 Redis에 같은 TTL로 원자 저장한다.
     * 기존 auth:refresh 키 형식은 유지해 현재 재발급 로직 및 운영 확인 명령과 호환되게 한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 회원 번호 (PK)
     * @param refreshToken 저장할 Refresh Token
     * @param userNick 알림 문구에 사용할 로그인 사용자 닉네임
     * @param ttlSeconds 토큰 유효 시간(초)
     */
    public void setLoginUserInfo(
            Long userNumb
          , String refreshToken
          , String userNick
          , Long ttlSeconds) {

        if (StringUtil.isEmpty(userNumb)
                || StringUtil.isEmpty(refreshToken)
                || StringUtil.isEmpty(ttlSeconds)
                || ttlSeconds <= 0) {
            throw new IllegalArgumentException("Login user Redis values are invalid.");
        }

        /*
         * 두 SET 사이에 장애가 나면 토큰과 닉네임의 존재 시간이 달라질 수 있다.
         * Lua 한 번으로 저장해 로그인 세션의 토큰과 표시 정보가 같은 시점에 생성되도록 보장한다.
         */
        redisTemplate.execute(
                SET_LOGIN_USER_SCRIPT
              , List.of(getRefreshTokenKey(userNumb), getUserNickKey(userNumb))
              , refreshToken
              , StringUtil.isEmpty(userNick) ? "" : userNick
              , String.valueOf(ttlSeconds)
        );
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
     * 로그인 사용자 번호로 Redis에 저장된 닉네임을 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return Redis에 저장된 닉네임, 로그인 정보가 없으면 null
     */
    public String getUserNick(Long userNumb) {
        if (StringUtil.isEmpty(userNumb)) {
            return null;
        }

        return redisTemplate.opsForValue().get(getUserNickKey(userNumb));
    }

    /**
     * 프로필 수정 후 Redis 닉네임을 갱신하되 Refresh Token의 남은 TTL을 그대로 적용한다.
     * 로그인 정보가 이미 만료된 사용자는 닉네임 캐시만 새로 만들지 않는다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 닉네임을 수정한 사용자 번호
     * @param userNick DB 수정이 완료된 최신 닉네임
     * @return 로그인 세션이 존재해 Redis 닉네임을 갱신했으면 true
     */
    public boolean uptUserNick(Long userNumb, String userNick) {
        if (StringUtil.isEmpty(userNumb) || StringUtil.isEmpty(userNick)) {
            return false;
        }

        Long updateCnt = redisTemplate.execute(
                UPDATE_USER_NICK_SCRIPT
              , List.of(getRefreshTokenKey(userNumb), getUserNickKey(userNumb))
              , userNick
        );

        return Long.valueOf(1L).equals(updateCnt);
    }

    /**
     * DB와 다른 오래된 닉네임을 알림에서 사용하지 않도록 닉네임 캐시만 제거한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 닉네임 캐시를 제거할 사용자 번호
     */
    public void delUserNick(Long userNumb) {
        if (StringUtil.isEmpty(userNumb)) {
            return;
        }

        redisTemplate.delete(getUserNickKey(userNumb));
    }

    /**
     * 로그아웃 시 Refresh Token과 로그인 사용자 닉네임을 함께 삭제한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 회원 번호 (PK)
     */
    public void delLoginUserInfo(Long userNumb) {
        if (StringUtil.isEmpty(userNumb)) {
            return;
        }

        redisTemplate.delete(List.of(getRefreshTokenKey(userNumb), getUserNickKey(userNumb)));
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
     * 로그인 사용자 닉네임 저장용 Redis Key를 생성한다. (형식: auth:user:nick:{userNumb})
     *
     * @author Seunghyeon.Kang
     * @param userNumb 회원 번호
     * @return Redis Key 문자열
     */
    private String getUserNickKey(Long userNumb) {
        return USER_NICK_PREFIX + userNumb;
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
