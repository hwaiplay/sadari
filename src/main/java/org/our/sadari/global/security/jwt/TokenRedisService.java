package org.our.sadari.global.security.jwt;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

/**
 * fileName       : TokenRedisService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-13
 * description    : 기기별 로그인 세션과 사용자 인증 캐시를 Redis에서 관리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-13        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    Redis Lua 주석 문법 수정
 * 2026-08-11        SeungHyeon.Kang    기기별 세션과 동시 Refresh Token 회전 추가
 */
@Service
@RequiredArgsConstructor
public class TokenRedisService {

    // 기기별 로그인 세션 Hash 접두사
    private static final String SESSION_PREFIX = "auth:session:";
    // 회원별 로그인 세션 Set 접두사
    private static final String USER_SESSION_PREFIX = "auth:user:sessions:";
    // 사용자 닉네임 캐시 접두사
    private static final String USER_NICK_PREFIX = "auth:user:nick:";
    // 사용자 상태 캐시 접두사
    private static final String USER_STATUS_PREFIX = "auth:user:status:";
    // 로그아웃된 Access Token 접두사
    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "auth:blacklist:access:";

    // 기기별 로그인 세션과 회원별 세션 색인을 원자적으로 생성하는 Lua 스크립트
    private static final String SET_LOGIN_USER_LUA = """
            redis.call('HSET', KEYS[1],
                'userNumb', ARGV[1],
                'current', ARGV[2],
                'previous', '',
                'graceUntil', '0')
            redis.call('EXPIRE', KEYS[1], ARGV[5])
            redis.call('SADD', KEYS[2], ARGV[3])
            redis.call('EXPIRE', KEYS[2], ARGV[5])
            if ARGV[4] == '' then
                redis.call('DEL', KEYS[3])
            else
                redis.call('SET', KEYS[3], ARGV[4], 'EX', ARGV[5])
            end
            redis.call('SET', KEYS[4], ARGV[6])
            return 1
            """;
    // 로그인 세션 생성 스크립트 객체
    private static final DefaultRedisScript<Long> SET_LOGIN_USER_SCRIPT =
            new DefaultRedisScript<>(SET_LOGIN_USER_LUA, Long.class);

    // 동시 재발급 요청이 이전 토큰을 제출하면 최초 회전 결과를 반환하는 Lua 스크립트
    private static final String ROTATE_REFRESH_LUA = """
            local owner = redis.call('HGET', KEYS[1], 'userNumb')
            if not owner or owner ~= ARGV[1] then
                return nil
            end
            local current = redis.call('HGET', KEYS[1], 'current')
            local previous = redis.call('HGET', KEYS[1], 'previous')
            local graceUntil = tonumber(redis.call('HGET', KEYS[1], 'graceUntil') or '0')
            local now = tonumber(redis.call('TIME')[1])
            if ARGV[2] == previous and now <= graceUntil then
                return current
            end
            if ARGV[2] ~= current then
                return nil
            end
            if previous ~= '' and now <= graceUntil then
                return current
            end
            redis.call('HSET', KEYS[1],
                'previous', current,
                'current', ARGV[3],
                'graceUntil', tostring(now + tonumber(ARGV[5])))
            redis.call('EXPIRE', KEYS[1], ARGV[4])
            redis.call('EXPIRE', KEYS[2], ARGV[4])
            return ARGV[3]
            """;
    // Refresh Token 회전 스크립트 객체
    private static final DefaultRedisScript<String> ROTATE_REFRESH_SCRIPT =
            new DefaultRedisScript<>(ROTATE_REFRESH_LUA, String.class);

    // 현재 세션을 제거하고 남은 세션이 없으면 공용 닉네임 캐시도 정리하는 Lua 스크립트
    private static final String DEL_LOGIN_SESSION_LUA = """
            redis.call('DEL', KEYS[1])
            redis.call('SREM', KEYS[2], ARGV[1])
            if redis.call('SCARD', KEYS[2]) == 0 then
                redis.call('DEL', KEYS[2])
                redis.call('DEL', KEYS[3])
            end
            return 1
            """;
    // 현재 세션 삭제 스크립트 객체
    private static final DefaultRedisScript<Long> DEL_LOGIN_SESSION_SCRIPT =
            new DefaultRedisScript<>(DEL_LOGIN_SESSION_LUA, Long.class);

    // JWT와 사용자 정보를 저장하는 Redis 연산 객체
    private final StringRedisTemplate redisTemplate;

    // 동시 Refresh Token 재발급 요청을 동일 회전 결과로 합칠 유예 시간
    @Value("${jwt.refresh-rotation-grace-in-seconds:10}")
    private long refreshRotationGraceSeconds;

    /**
     * 기기별 Refresh Token 세션과 회원 공용 표시 정보를 Redis에 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호
     * @param sessionId 기기별 로그인 세션 식별자
     * @param refreshToken 저장할 Refresh Token
     * @param userNick 알림 문구에 사용할 닉네임
     * @param userStat 현재 회원 상태
     * @param ttlSeconds Refresh Token 유효 시간
     */
    public void setLoginUserInfo(Long userNumb, String sessionId, String refreshToken
                               , String userNick, String userStat, Long ttlSeconds) {
        // 필수 인증 값이나 만료 시간이 올바르지 않으면 불완전한 세션 생성을 차단한다
        if (StringUtil.isEmpty(userNumb) || StringUtil.isEmpty(sessionId)
                || StringUtil.isEmpty(refreshToken) || StringUtil.isEmpty(userStat)
                || StringUtil.isEmpty(ttlSeconds) || ttlSeconds <= 0) {
            throw new IllegalArgumentException("Login user Redis values are invalid.");
        }

        // 세션 Hash와 사용자 세션 색인 및 공용 캐시를 한 번의 Redis 명령으로 반영한다
        redisTemplate.execute(
                SET_LOGIN_USER_SCRIPT
              , List.of(getSessionKey(sessionId), getUserSessionKey(userNumb)
                      , getUserNickKey(userNumb), getUserStatusKey(userNumb))
              , String.valueOf(userNumb)
              , refreshToken
              , sessionId
              , StringUtil.isEmpty(userNick) ? "" : userNick
              , String.valueOf(ttlSeconds)
              , userStat
        );
    }

    /**
     * 제출된 Refresh Token을 원자적으로 회전하고 동시 요청에는 같은 최신 토큰을 반환한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호
     * @param sessionId 기기별 로그인 세션 식별자
     * @param presentedToken 브라우저가 제출한 Refresh Token
     * @param proposedToken 새로 발급한 Refresh Token 후보
     * @param ttlSeconds 새 Refresh Token 유효 시간
     * @return 저장된 최신 Refresh Token, 검증 실패 시 null
     */
    public String rotateRefreshToken(Long userNumb, String sessionId, String presentedToken
                                   , String proposedToken, long ttlSeconds) {
        // 세션 회전에 필요한 값이 없으면 검증 실패로 반환한다
        if (StringUtil.isEmpty(userNumb) || StringUtil.isEmpty(sessionId)
                || StringUtil.isEmpty(presentedToken) || StringUtil.isEmpty(proposedToken)
                || ttlSeconds <= 0 || refreshRotationGraceSeconds < 0) {
            // 회전할 세션이 없음을 반환한다
            return null;
        }

        // Redis 서버 시각 기준의 유예 구간 안에서는 여러 탭과 서비스 워커에 같은 최신 토큰을 반환한다
        return redisTemplate.execute(
                ROTATE_REFRESH_SCRIPT
              , List.of(getSessionKey(sessionId), getUserSessionKey(userNumb))
              , String.valueOf(userNumb)
              , presentedToken
              , proposedToken
              , String.valueOf(ttlSeconds)
              , String.valueOf(refreshRotationGraceSeconds)
        );
    }

    /**
     * Access Token의 회원과 세션 식별자가 현재 활성 세션을 가리키는지 확인한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 토큰의 회원 번호
     * @param sessionId 토큰의 세션 식별자
     * @return 활성 세션 소유권 일치 여부
     */
    public boolean isSessionActive(Long userNumb, String sessionId) {
        // 식별값이 없으면 세션을 활성 상태로 인정하지 않는다
        if (StringUtil.isEmpty(userNumb) || StringUtil.isEmpty(sessionId)) {
            // 활성 세션이 아님을 반환한다
            return false;
        }

        // 세션 Hash에 기록한 소유 회원 번호와 토큰의 회원 번호가 같은지 확인한다
        Object savedUserNumb = redisTemplate.opsForHash().get(getSessionKey(sessionId), "userNumb");
        // 동일 회원의 만료되지 않은 세션일 때만 인증을 허용한다
        return String.valueOf(userNumb).equals(savedUserNumb);
    }

    /**
     * 로그인 회원의 현재 이용 상태를 Redis에서 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 회원 번호
     * @return 회원 상태 코드
     */
    public String getUserStatus(Long userNumb) {
        // 회원 번호가 없으면 상태 캐시를 조회하지 않는다
        if (StringUtil.isEmpty(userNumb)) {
            // 조회할 회원 상태가 없음을 반환한다
            return null;
        }

        // 로그인 세션 삭제와 분리된 회원 상태 캐시를 반환한다
        return redisTemplate.opsForValue().get(getUserStatusKey(userNumb));
    }

    /**
     * 로그인 세션 유무와 관계없이 회원 상태 캐시를 최신 값으로 변경한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 상태를 변경할 회원 번호
     * @param userStat 변경할 회원 상태
     */
    public void uptUserStatus(Long userNumb, String userStat) {
        // 회원 번호나 상태가 없으면 Redis 상태를 변경하지 않는다
        if (StringUtil.isEmpty(userNumb) || StringUtil.isEmpty(userStat)) {
            // 변경할 상태가 없어 처리를 종료한다
            return;
        }

        // 로그아웃이 계정 제한 상태를 삭제하지 못하도록 만료 없는 별도 키로 저장한다
        redisTemplate.opsForValue().set(getUserStatusKey(userNumb), userStat);
    }

    /**
     * 로그인 사용자 번호로 Redis에 저장된 닉네임을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return Redis 닉네임
     */
    public String getUserNick(Long userNumb) {
        // 회원 번호가 없으면 닉네임 캐시를 조회하지 않는다
        if (StringUtil.isEmpty(userNumb)) {
            // 조회할 닉네임이 없음을 반환한다
            return null;
        }

        // 로그인 사용자 번호에 대응하는 닉네임 캐시를 반환한다
        return redisTemplate.opsForValue().get(getUserNickKey(userNumb));
    }

    /**
     * 활성 로그인 세션의 남은 최대 TTL 동안 닉네임 캐시를 갱신한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 닉네임을 수정한 사용자 번호
     * @param userNick DB 수정이 완료된 최신 닉네임
     * @return 활성 세션이 있어 캐시를 갱신했으면 true
     */
    public boolean uptUserNick(Long userNumb, String userNick) {
        // 필수 값이 없으면 닉네임 캐시를 만들지 않는다
        if (StringUtil.isEmpty(userNumb) || StringUtil.isEmpty(userNick)) {
            // 닉네임을 갱신하지 못했음을 반환한다
            return false;
        }

        // 회원별 세션 색인의 남은 시간을 조회한다
        Long ttlSeconds = redisTemplate.getExpire(getUserSessionKey(userNumb));
        // 활성 세션이 없으면 닉네임 캐시만 새로 만들지 않는다
        if (StringUtil.isEmpty(ttlSeconds) || ttlSeconds <= 0) {
            // 닉네임을 갱신하지 못했음을 반환한다
            return false;
        }

        // 최신 닉네임을 현재 로그인 유지시간 동안 저장한다
        redisTemplate.opsForValue().set(getUserNickKey(userNumb), userNick, Duration.ofSeconds(ttlSeconds));
        // 닉네임을 갱신했음을 반환한다
        return true;
    }

    /**
     * DB와 다른 오래된 닉네임 캐시만 제거한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 닉네임 캐시를 제거할 사용자 번호
     */
    public void delUserNick(Long userNumb) {
        // 회원 번호가 없으면 삭제하지 않는다
        if (StringUtil.isEmpty(userNumb)) {
            // 삭제 대상을 찾을 수 없어 종료한다
            return;
        }

        // 공용 닉네임 캐시를 제거한다
        redisTemplate.delete(getUserNickKey(userNumb));
    }

    /**
     * 현재 기기의 로그인 세션만 제거한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그아웃 회원 번호
     * @param sessionId 현재 기기 세션 식별자
     */
    public void delLoginSession(Long userNumb, String sessionId) {
        // 세션 소유권을 특정할 수 없으면 삭제하지 않는다
        if (StringUtil.isEmpty(userNumb) || StringUtil.isEmpty(sessionId)) {
            // 삭제 대상을 찾을 수 없어 종료한다
            return;
        }

        // 해당 세션과 회원별 색인 항목을 원자적으로 제거한다
        redisTemplate.execute(
                DEL_LOGIN_SESSION_SCRIPT
              , List.of(getSessionKey(sessionId), getUserSessionKey(userNumb), getUserNickKey(userNumb))
              , sessionId
        );
    }

    /**
     * 회원의 모든 기기 로그인 세션과 닉네임 캐시를 제거한다.
     * 계정 제한 상태는 별도 키에 남겨 로그아웃으로 우회할 수 없게 한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 전체 로그아웃 회원 번호
     */
    public void delLoginUserInfo(Long userNumb) {
        // 회원 번호가 없으면 세션을 삭제하지 않는다
        if (StringUtil.isEmpty(userNumb)) {
            // 삭제 대상을 찾을 수 없어 종료한다
            return;
        }

        // 회원별 세션 식별자 목록을 조회한다
        Set<String> sessionIdSet = redisTemplate.opsForSet().members(getUserSessionKey(userNumb));
        // 조회된 모든 기기 세션 Hash를 제거한다
        if (sessionIdSet != null && !sessionIdSet.isEmpty()) {
            // 각 세션 식별자를 실제 Redis 키로 변환해 일괄 삭제한다
            redisTemplate.delete(sessionIdSet.stream().map(this::getSessionKey).toList());
        }

        // 세션 색인과 세션 종속 닉네임 캐시를 제거한다
        redisTemplate.delete(List.of(getUserSessionKey(userNumb), getUserNickKey(userNumb)));
    }

    /**
     * 물리 삭제 회원의 세션과 모든 인증 캐시를 제거한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 물리 삭제 회원 번호
     */
    public void delAllUserInfo(Long userNumb) {
        // 회원의 모든 기기 세션을 먼저 제거한다
        delLoginUserInfo(userNumb);
        // 회원 원본이 사라진 뒤 남을 수 없는 상태 캐시도 물리 제거한다
        if (!StringUtil.isEmpty(userNumb)) {
            redisTemplate.delete(getUserStatusKey(userNumb));
        }
    }

    /**
     * 로그아웃 처리된 Access Token의 식별자를 남은 유효시간 동안 차단한다.
     *
     * @author SeungHyeon.Kang
     * @param tokenId Access Token 식별자
     * @param ttlSeconds 남은 유효 시간
     */
    public void setAccessTokenBlacklist(String tokenId, long ttlSeconds) {
        // 토큰 식별자나 유효 시간이 없으면 블랙리스트에 등록하지 않는다
        if (StringUtil.isEmpty(tokenId) || ttlSeconds <= 0) {
            // 등록할 토큰이 없어 종료한다
            return;
        }

        // Access Token이 만료될 때까지만 로그아웃 표식을 저장한다
        redisTemplate.opsForValue().set(getTokenBlacklistKey(tokenId), "logout", Duration.ofSeconds(ttlSeconds));
    }

    /**
     * Access Token 식별자가 로그아웃 블랙리스트에 존재하는지 확인한다.
     *
     * @author SeungHyeon.Kang
     * @param tokenId Access Token 식별자
     * @return 블랙리스트 등록 여부
     */
    public boolean hasAccessTokenBlacklist(String tokenId) {
        // 식별자가 없으면 블랙리스트 조회를 수행하지 않는다
        if (StringUtil.isEmpty(tokenId)) {
            // 블랙리스트 대상이 아님을 반환한다
            return false;
        }

        // 로그아웃 표식 키가 존재하는지 반환한다
        return Boolean.TRUE.equals(redisTemplate.hasKey(getTokenBlacklistKey(tokenId)));
    }

    /**
     * 기기별 로그인 세션 Redis 키를 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param sessionId 기기별 로그인 세션 식별자
     * @return 세션 Hash Redis 키
     */
    private String getSessionKey(String sessionId) {
        // 세션 식별자를 포함한 Redis 키를 반환한다
        return SESSION_PREFIX + sessionId;
    }

    /**
     * 회원별 로그인 세션 색인 Redis 키를 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호
     * @return 회원별 세션 Set Redis 키
     */
    private String getUserSessionKey(Long userNumb) {
        // 회원 번호를 포함한 세션 색인 Redis 키를 반환한다
        return USER_SESSION_PREFIX + userNumb;
    }

    /**
     * 회원 닉네임 Redis 키를 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호
     * @return 회원 닉네임 Redis 키
     */
    private String getUserNickKey(Long userNumb) {
        // 회원 번호를 포함한 닉네임 Redis 키를 반환한다
        return USER_NICK_PREFIX + userNumb;
    }

    /**
     * 회원 상태 Redis 키를 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호
     * @return 회원 상태 Redis 키
     */
    private String getUserStatusKey(Long userNumb) {
        // 회원 번호를 포함한 상태 Redis 키를 반환한다
        return USER_STATUS_PREFIX + userNumb;
    }

    /**
     * Access Token 블랙리스트 Redis 키를 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param tokenId Access Token 식별자
     * @return Access Token 블랙리스트 Redis 키
     */
    private String getTokenBlacklistKey(String tokenId) {
        // Access Token 식별자를 포함한 블랙리스트 Redis 키를 반환한다
        return ACCESS_TOKEN_BLACKLIST_PREFIX + tokenId;
    }
}
