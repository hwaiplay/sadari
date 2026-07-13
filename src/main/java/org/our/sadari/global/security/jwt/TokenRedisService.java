package org.our.sadari.global.security.jwt;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenRedisService {

    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";
    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "auth:blacklist:access:";

    private final StringRedisTemplate redisTemplate;

    /**
     * 로그인 또는 토큰 재발급 과정에서 발급된 refreshToken을 사용자 번호 기준 Redis 키에 저장한다.
     * JWT 자체의 만료 시간과 Redis 만료 시간을 동일하게 맞춰 토큰이 만료되면 Redis 데이터도 자동으로 제거되게 한다.
     * 같은 사용자가 다시 로그인하거나 refreshToken rotation이 일어나면 동일 키에 새 토큰을 덮어써 이전 토큰을 무효화한다.
     * @Author Seunghyeon.Kang
     * @param userNumb refresh token의 소유자를 식별하는 사용자 고유 번호
     * @param refreshToken Redis에 저장할 서버 발급 refreshToken 문자열
     * @param ttlSeconds Redis에 저장된 refreshToken을 유지할 초 단위 만료 시간
     * @return
     */
    public void setRefreshToken(Long userNumb, String refreshToken, long ttlSeconds) {
        redisTemplate.opsForValue().set(
                getRefreshTokenKey(userNumb),
                refreshToken,
                Duration.ofSeconds(ttlSeconds)
        );
    }

    /**
     * 사용자 번호 기준으로 Redis에 저장된 refreshToken을 조회한다.
     * 재발급 요청에서 쿠키로 들어온 refreshToken과 Redis에 저장된 값을 비교하기 위해 사용한다.
     * 조회 결과가 없으면 로그아웃되었거나, 만료되었거나, 서버가 인정하지 않는 토큰으로 판단한다.
     * @Author Seunghyeon.Kang
     * @param userNumb refresh token을 조회할 사용자 고유 번호
     * @return Redis에 저장되어 있는 refreshToken 문자열
     */
    public String getRefreshToken(Long userNumb) {
        return redisTemplate.opsForValue().get(getRefreshTokenKey(userNumb));
    }

    /**
     * 사용자 번호 기준으로 Redis에 저장된 refreshToken을 삭제한다.
     * 로그아웃 시 재발급 권한을 즉시 차단하기 위해 사용하며, 삭제 이후 같은 refreshToken으로는 accessToken을 다시 받을 수 없다.
     * 이미 만료되었거나 존재하지 않는 키를 삭제하는 경우에도 예외 없이 Redis 삭제 명령만 수행한다.
     * @Author Seunghyeon.Kang
     * @param userNumb refresh token을 삭제할 사용자 고유 번호
     * @return
     */
    public void deleteRefreshToken(Long userNumb) {
        redisTemplate.delete(getRefreshTokenKey(userNumb));
    }

    /**
     * 로그아웃된 accessToken의 JWT ID를 Redis blacklist에 등록한다.
     * accessToken은 원래 무상태 방식이라 서버 저장소 없이도 만료 전까지 유효하지만, 이 blacklist를 통해 로그아웃 직후 즉시 차단한다.
     * Redis 만료 시간은 accessToken의 남은 만료 시간과 동일하게 설정하여 토큰이 자연 만료된 뒤에는 blacklist 데이터도 자동 정리되게 한다.
     * @Author Seunghyeon.Kang
     * @param tokenId blacklist에 등록할 accessToken의 JWT ID 값
     * @param ttlSeconds accessToken이 만료될 때까지 남은 초 단위 시간
     * @return
     */
    public void setAccessTokenBlacklist(String tokenId, long ttlSeconds) {
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
     * accessToken의 JWT ID가 Redis blacklist에 등록되어 있는지 확인한다.
     * JWT 서명과 만료 시간이 정상이어도 blacklist에 존재하면 로그아웃된 토큰으로 보고 인증을 허용하지 않는다.
     * tokenId가 비어 있으면 blacklist 판단 대상이 아니므로 false를 반환한다.
     * @Author Seunghyeon.Kang
     * @param tokenId blacklist 등록 여부를 확인할 accessToken의 JWT ID 값
     * @return blacklist에 등록되어 차단해야 하는 토큰이면 true, 아니면 false
     */
    public boolean hasAccessTokenBlacklist(String tokenId) {
        if (StringUtil.isEmpty(tokenId)) {
            return false;
        }

        return Boolean.TRUE.equals(redisTemplate.hasKey(getAccessTokenBlacklistKey(tokenId)));
    }

    /**
     * 사용자 번호를 기준으로 refreshToken 저장용 Redis 키를 생성한다.
     * refreshToken은 사용자당 하나만 유효하게 유지하는 정책이므로 사용자 번호를 key suffix로 사용한다.
     * 같은 사용자가 여러 번 로그인하면 기존 refreshToken 값은 동일 키에서 새 값으로 교체된다.
     * @Author Seunghyeon.Kang
     * @param userNumb Redis 키에 포함할 사용자 고유 번호
     * @return refreshToken 저장과 조회에 사용할 Redis 키 문자열
     */
    private String getRefreshTokenKey(Long userNumb) {
        return REFRESH_TOKEN_PREFIX + userNumb;
    }

    /**
     * accessToken blacklist 저장용 Redis 키를 생성한다.
     * token 전체 문자열 대신 JWT ID를 사용해 key 길이를 줄이고, 로그아웃된 accessToken만 만료 시간 동안 추적한다.
     * 해당 키가 존재하는 동안 같은 JWT ID를 가진 accessToken은 인증 필터에서 차단된다.
     * @Author Seunghyeon.Kang
     * @param tokenId Redis 키에 포함할 accessToken의 JWT ID 값
     * @return accessToken blacklist 저장과 조회에 사용할 Redis 키 문자열
     */
    private String getAccessTokenBlacklistKey(String tokenId) {
        return ACCESS_TOKEN_BLACKLIST_PREFIX + tokenId;
    }
}
