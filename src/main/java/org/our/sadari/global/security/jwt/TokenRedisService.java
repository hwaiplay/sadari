package org.our.sadari.global.security.jwt;

import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

/**
 * fileName       : TokenRedisService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-13
 * description    : 인증과 보안 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-13        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    Redis Lua 주석 문법 수정
 */
@Service
@RequiredArgsConstructor
public class TokenRedisService {
    // REFRESH TOKEN 접두사 설정값
    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";
    // USER NICK 접두사 설정값
    private static final String USER_NICK_PREFIX = "auth:user:nick:";
    // 접근 TOKEN BLACKLIST 접두사 설정값
    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "auth:blacklist:access:";

    // 로그인 사용자 정보를 원자적으로 저장하는 Lua 스크립트
    private static final String SET_LOGIN_USER_LUA = """
            -- Refresh Token을 지정된 로그인 유지시간 동안 저장한다
            redis.call('SET', KEYS[1], ARGV[1], 'EX', ARGV[3])
            -- 닉네임이 비어 있으면 이전 로그인에서 남은 닉네임을 제거한다
            if ARGV[2] == '' then
                redis.call('DEL', KEYS[2])
            else
                -- 닉네임이 있으면 Refresh Token과 같은 만료시간으로 저장한다
                redis.call('SET', KEYS[2], ARGV[2], 'EX', ARGV[3])
            end
            -- 두 로그인 정보를 정상적으로 반영한 결과를 반환한다
            return 1
            """;
    // 로그인 사용자 정보를 저장하는 Redis 스크립트 객체
    private static final DefaultRedisScript<Long> SET_LOGIN_USER_SCRIPT = new DefaultRedisScript<>(SET_LOGIN_USER_LUA, Long.class);

    // 로그인 사용자의 닉네임을 원자적으로 갱신하는 Lua 스크립트
    private static final String UPDATE_USER_NICK_LUA = """
            -- 로그인 세션과 동일한 만료시간을 적용하기 위해 Refresh Token의 남은 시간을 조회한다
            local refreshTtl = redis.call('TTL', KEYS[1])
            -- 로그인 세션이 없거나 만료됐으면 닉네임 캐시도 남기지 않는다
            if refreshTtl <= 0 then
                redis.call('DEL', KEYS[2])
                -- 갱신할 로그인 세션이 없음을 반환한다
                return 0
            end
            -- 수정된 닉네임을 Refresh Token의 남은 만료시간으로 저장한다
            redis.call('SET', KEYS[2], ARGV[1], 'EX', refreshTtl)
            -- 닉네임 캐시를 정상적으로 갱신한 결과를 반환한다
            return 1
            """;
    // 로그인 사용자의 닉네임을 갱신하는 Redis 스크립트 객체
    private static final DefaultRedisScript<Long> UPDATE_USER_NICK_SCRIPT = new DefaultRedisScript<>(UPDATE_USER_NICK_LUA, Long.class);

    // JWT와 사용자 정보를 저장하는 Redis 연산 객체
    private final StringRedisTemplate redisTemplate;

    /**
     * 로그인 사용자의 Refresh Token과 닉네임을 Redis에 같은 TTL로 원자 저장한다.
     * 기존 auth:refresh 키 형식은 유지해 현재 재발급 로직 및 운영 확인 명령과 호환되게 한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호 (PK)
     * @param refreshToken 저장할 Refresh Token
     * @param userNick 알림 문구에 사용할 로그인 사용자 닉네임
     * @param ttlSeconds 토큰 유효 시간(초)
     */
    public void setLoginUserInfo(Long userNumb, String refreshToken, String userNick
                               , Long ttlSeconds) {
        // userNumb 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(userNumb) || StringUtil.isEmpty(refreshToken)
                || StringUtil.isEmpty(ttlSeconds) || ttlSeconds <= 0) {

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
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호 (PK)
     * @return 저장된 Refresh Token (없을 경우 null)
     */
    public String getRefreshToken(Long userNumb) {
        // 회원 번호로 Redis에 저장된 Refresh Token을 조회 결과를 반환한다
        return redisTemplate.opsForValue().get(getRefreshTokenKey(userNumb));
    }

    /**
     * 로그인 사용자 번호로 Redis에 저장된 닉네임을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return Redis에 저장된 닉네임, 로그인 정보가 없으면 null
     */
    public String getUserNick(Long userNumb) {
        // userNumb 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(userNumb)) {
            // 조회하거나 생성할 값이 없음을 반환한다
            return null;
        }

        // 로그인 사용자 번호로 Redis에 저장된 닉네임을 조회 결과를 반환한다
        return redisTemplate.opsForValue().get(getUserNickKey(userNumb));
    }

    /**
     * 프로필 수정 후 Redis 닉네임을 갱신하되 Refresh Token의 남은 TTL을 그대로 적용한다.
     * 로그인 정보가 이미 만료된 사용자는 닉네임 캐시만 새로 만들지 않는다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 닉네임을 수정한 사용자 번호
     * @param userNick DB 수정이 완료된 최신 닉네임
     * @return 로그인 세션이 존재해 Redis 닉네임을 갱신했으면 true
     */
    public boolean uptUserNick(Long userNumb, String userNick) {
        // userNumb 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(userNumb) || StringUtil.isEmpty(userNick)) {
            // 프로필 수정 후 Redis 닉네임을 갱신하되 Refresh Token의 남은 TTL을 그대로 적용 판정값을 반환한다
            return false;
        }

        // Redis 키 패턴에 해당하는 사용자 캐시를 일괄 갱신한다
        Long updateCnt = redisTemplate.execute(
                UPDATE_USER_NICK_SCRIPT
              , List.of(getRefreshTokenKey(userNumb), getUserNickKey(userNumb))
              , userNick
        );
        // 프로필 수정 후 Redis 닉네임을 갱신하되 Refresh Token의 남은 TTL을 그대로 적용 결과를 반환한다
        return Long.valueOf(1L).equals(updateCnt);
    }

    /**
     * DB와 다른 오래된 닉네임을 알림에서 사용하지 않도록 닉네임 캐시만 제거한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 닉네임 캐시를 제거할 사용자 번호
     */
    public void delUserNick(Long userNumb) {
        // userNumb 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(userNumb)) {
            // DB와 다른 오래된 닉네임을 알림에서 사용하지 않도록 닉네임 캐시만 제거 결과를 반환한다
            return;
        }

        // 더 이상 유효하지 않은 데이터를 삭제한다
        redisTemplate.delete(getUserNickKey(userNumb));
    }

    /**
     * 로그아웃 시 Refresh Token과 로그인 사용자 닉네임을 함께 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호 (PK)
     */
    public void delLoginUserInfo(Long userNumb) {
        // userNumb 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(userNumb)) {
            // 로그아웃 시 Refresh Token과 로그인 사용자 닉네임을 함께 삭제 결과를 반환한다
            return;
        }

        // 더 이상 유효하지 않은 데이터를 삭제한다
        redisTemplate.delete(List.of(getRefreshTokenKey(userNumb), getUserNickKey(userNumb)));
    }

    /**
     * 로그아웃 처리된 Access Token의 식별자(jti)를 Redis 블랙리스트에 등록한다.
     *
     * @author SeungHyeon.Kang
     * @param tokenId Access Token의 고유 식별자 (jti)
     * @param ttlSeconds Access Token의 남은 유효 시간(초)
     */
    public void setAccessTokenBlacklist(String tokenId, long ttlSeconds) {
        // 토큰 식별자가 없거나 만료 시간이 유효하지 않은(0 이하) 경우 블랙리스트에 등록하지 않고 종료한다.
        if (StringUtil.isEmpty(tokenId) || ttlSeconds <= 0) {
            // 로그아웃 처리된 Access Token의 식별자(jti)를 Redis 블랙리스트에 등록 결과를 반환한다
            return;
        }

        // 문자열 형태의 Redis 값을 처리할 연산 객체를 조회한다
        redisTemplate.opsForValue().set(getAccessTokenBlacklistKey(tokenId), "logout", Duration.ofSeconds(ttlSeconds));
    }

    /**
     * 전달받은 Access Token 식별자(jti)가 Redis 블랙리스트에 존재하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     * @param tokenId Access Token의 고유 식별자 (jti)
     * @return 블랙리스트 등록 여부 (true: 로그아웃된 토큰, false: 사용 가능한 토큰)
     */
    public boolean hasAccessTokenBlacklist(String tokenId) {
        // 토큰 식별자(jti)가 전달되지 않은 경우 정상적인 조회 불가로 판단하여 false를 반환한다.
        if (StringUtil.isEmpty(tokenId)) {
            // 전달받은 Access Token 식별자(jti)가 Redis 블랙리스트에 존재하는지 검증 판정값을 반환한다
            return false;
        }

        // 전달받은 Access Token 식별자(jti)가 Redis 블랙리스트에 존재하는지 검증 결과를 반환한다
        return Boolean.TRUE.equals(redisTemplate.hasKey(getAccessTokenBlacklistKey(tokenId)));
    }

    /**
     * Refresh Token 저장용 Redis Key를 생성한다. (형식: auth:refresh:{userNumb})
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호 (PK)
     * @return Redis Key 문자열
     */
    private String getRefreshTokenKey(Long userNumb) {
        // Refresh Token 저장용 Redis Key를 생성한다. (형식: auth:refresh:{userNumb}) 결과를 반환한다
        return REFRESH_TOKEN_PREFIX + userNumb;
    }

    /**
     * 로그인 사용자 닉네임 저장용 Redis Key를 생성한다. (형식: auth:user:nick:{userNumb})
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호
     * @return Redis Key 문자열
     */
    private String getUserNickKey(Long userNumb) {
        // 로그인 사용자 닉네임 저장용 Redis Key를 생성한다. (형식: auth:user:nick:{userNumb}) 결과를 반환한다
        return USER_NICK_PREFIX + userNumb;
    }

    /**
     * Access Token 블랙리스트 저장용 Redis Key를 생성한다. (형식: auth:blacklist:access:{tokenId})
     *
     * @author SeungHyeon.Kang
     * @param tokenId Access Token 고유 식별자 (jti)
     * @return Redis Key 문자열
     */
    private String getAccessTokenBlacklistKey(String tokenId) {
        // Access Token 블랙리스트 저장용 Redis Key를 생성한다. (형식: auth:blacklist:access:{tokenId}) 결과를 반환한다
        return ACCESS_TOKEN_BLACKLIST_PREFIX + tokenId;
    }
}
