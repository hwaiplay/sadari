package org.our.sadari.book.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.book.dto.KakaoBookJsonDto;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

/**
 * fileName       : BookSearchProtectionService
 * author         : SeungHyeon.Kang
 * date           : 2026-08-16
 * description    : Redis로 도서 검색 호출 제한과 공용 검색 결과 캐시를 관리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-16        SeungHyeon.Kang    최초 생성
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookSearchProtectionService {

    // 회원별 분간 및 일간 요청 제한을 함께 검사하고 허용된 요청만 원자 증가시키는 Lua 스크립트
    private static final String REQUEST_LIMIT_LUA = """
            local minuteCount = tonumber(redis.call('GET', KEYS[1]) or '0')
            local dailyCount = tonumber(redis.call('GET', KEYS[2]) or '0')
            if minuteCount >= tonumber(ARGV[1]) or dailyCount >= tonumber(ARGV[2]) then
                return 0
            end
            minuteCount = redis.call('INCR', KEYS[1])
            if minuteCount == 1 then
                redis.call('EXPIRE', KEYS[1], tonumber(ARGV[3]))
            end
            dailyCount = redis.call('INCR', KEYS[2])
            if dailyCount == 1 then
                redis.call('EXPIRE', KEYS[2], tonumber(ARGV[4]))
            end
            return 1
            """;
    // 앱 전체 카카오 호출 보호 한도를 넘지 않은 요청만 원자 증가시키는 Lua 스크립트
    private static final String PROVIDER_LIMIT_LUA = """
            local providerCount = tonumber(redis.call('GET', KEYS[1]) or '0')
            if providerCount >= tonumber(ARGV[1]) then
                return 0
            end
            providerCount = redis.call('INCR', KEYS[1])
            if providerCount == 1 then
                redis.call('EXPIRE', KEYS[1], tonumber(ARGV[2]))
            end
            return 1
            """;
    // 회원별 분간 도서 검색 요청 횟수 Redis 키 접두사
    private static final String MINUTE_LIMIT_KEY_PREFIX = "book:search:rate:minute:";
    // 회원별 일간 도서 검색 요청 횟수 Redis 키 접두사
    private static final String DAILY_LIMIT_KEY_PREFIX = "book:search:rate:day:";
    // 앱 전체 카카오 도서 검색 실제 호출 횟수 Redis 키
    private static final String PROVIDER_LIMIT_KEY = "book:search:provider:day";
    // 검색어 원문을 노출하지 않는 공용 검색 결과 Redis 키 접두사
    private static final String SEARCH_CACHE_KEY_PREFIX = "book:search:cache:";
    // 회원별 제한 Lua 실행 객체
    private static final DefaultRedisScript<Long> REQUEST_LIMIT_SCRIPT =
            new DefaultRedisScript<>(REQUEST_LIMIT_LUA, Long.class);
    // 앱 전체 외부 호출 제한 Lua 실행 객체
    private static final DefaultRedisScript<Long> PROVIDER_LIMIT_SCRIPT =
            new DefaultRedisScript<>(PROVIDER_LIMIT_LUA, Long.class);
    // Lua 스크립트의 요청 허용 결과값
    private static final long REQUEST_ALLOWED = 1L;
    // 분간 제한 Redis 키 유효시간
    private static final int MINUTE_LIMIT_TTL_SECONDS = 60;
    // 일간 제한 Redis 키 유효시간
    private static final int DAILY_LIMIT_TTL_SECONDS = 86400;

    // 회원 한 명의 60초간 최대 도서 검색 요청 수
    @Value("${book.search.rate-limit-per-minute:20}")
    private int rateLimitPerMinute;
    // 회원 한 명의 24시간 최대 도서 검색 요청 수
    @Value("${book.search.rate-limit-per-day:200}")
    private int rateLimitPerDay;
    // 앱 전체의 24시간 최대 카카오 도서 검색 실제 호출 수
    @Value("${book.search.provider-call-limit-per-day:27000}")
    private int providerCallLimitPerDay;
    // 공용 도서 검색 결과 Redis 캐시 유효시간
    @Value("${book.search.cache-ttl-seconds:600}")
    private int cacheTtlSeconds;

    // 도서 검색 제한과 캐시를 공유할 Redis 문자열 연산 객체
    private final StringRedisTemplate redisTemplate;
    // 카카오 도서 검색 캐시 직렬화 객체
    private final ObjectMapper objectMapper;

    /**
     * 회원별 분간 및 일간 검색 제한을 원자적으로 검사하고 요청 횟수를 반영한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 도서 검색을 요청한 로그인 회원 번호
     * @return 두 제한을 모두 통과한 요청 여부
     */
    public boolean isRequestAllowed(Long userNumb) {
        // 인증되지 않은 요청은 외부 API 쿼터를 사용할 수 없도록 차단한다
        if (StringUtil.isEmpty(userNumb)) {
            // 회원 식별값이 없는 요청을 거절한다
            return false;
        }

        // Redis 장애 시 카카오 쿼터가 무방비로 소모되지 않도록 검색 요청을 차단한다
        try {
            // 한 회원의 분간 및 일간 제한을 하나의 Redis 명령으로 검사한다
            Long result = redisTemplate.execute(
                    REQUEST_LIMIT_SCRIPT
                  , List.of(getMinuteLimitKey(userNumb), getDailyLimitKey(userNumb))
                  , String.valueOf(rateLimitPerMinute), String.valueOf(rateLimitPerDay)
                  , String.valueOf(MINUTE_LIMIT_TTL_SECONDS), String.valueOf(DAILY_LIMIT_TTL_SECONDS)
            );
            // Redis가 명시적으로 허용한 요청만 카카오 검색 후보로 반환한다
            return !StringUtil.isEmpty(result) && result == REQUEST_ALLOWED;
        }

        // 검색 제한 저장소 장애는 비밀값 없이 운영 로그에 기록한다
        catch (RuntimeException e) {
            // Redis 제한을 확인하지 못한 원인을 예외 정보와 함께 기록한다
            log.error("도서 검색 회원별 요청 제한을 확인하지 못했습니다.", e);
            // 제한을 확인하지 못한 요청을 카카오 호출 전에 차단한다
            return false;
        }
    }

    /**
     * 앱 전체 카카오 도서 검색 실제 호출 한도를 예약한다
     *
     * @author SeungHyeon.Kang
     * @return 비상 여유를 제외한 외부 호출 한도 안에서 예약된 요청 여부
     */
    public boolean reserveProviderCall() {
        // Redis 장애 시 카카오 일일 쿼터 보호 한도를 우회하지 않도록 차단한다
        try {
            // 앱 전체 실제 호출 횟수를 Redis에서 원자적으로 검사하고 증가시킨다
            Long result = redisTemplate.execute(
                    PROVIDER_LIMIT_SCRIPT
                  , List.of(PROVIDER_LIMIT_KEY)
                  , String.valueOf(providerCallLimitPerDay), String.valueOf(DAILY_LIMIT_TTL_SECONDS)
            );
            // Redis가 비상 여유 안에서 예약한 외부 호출만 허용한다
            return !StringUtil.isEmpty(result) && result == REQUEST_ALLOWED;
        }

        // 앱 전체 보호 카운터 장애는 쿼터 소모 없이 운영 로그로 남긴다
        catch (RuntimeException e) {
            // Redis 외부 호출 한도를 확인하지 못한 원인을 예외 정보와 함께 기록한다
            log.error("카카오 도서 검색 일일 호출 한도를 확인하지 못했습니다.", e);
            // 보호 한도를 확인하지 못한 카카오 호출을 차단한다
            return false;
        }
    }

    /**
     * 검색어와 카카오 페이지가 같은 공용 검색 결과를 Redis에서 조회한다
     *
     * @author SeungHyeon.Kang
     * @param query 사용자가 입력한 도서 검색어
     * @param page 카카오 도서 검색 페이지 번호
     * @return 역직렬화된 카카오 도서 검색 결과 또는 캐시 누락값
     */
    public KakaoBookJsonDto getCachedSearch(String query, int page) {
        // Redis 조회나 캐시 역직렬화 오류가 원문 검색어를 노출하지 않도록 공통 실패 경로로 격리한다
        try {
            // 검색어 해시와 페이지로 구성한 공용 캐시 값을 조회한다
            String cachedJson = redisTemplate.opsForValue().get(getCacheKey(query, page));

            // 저장된 검색 결과가 없으면 카카오 API 호출이 필요함을 반환한다
            if (StringUtil.isEmpty(cachedJson)) {
                // 공용 검색 캐시 누락값을 반환한다
                return null;
            }

            // 캐시 JSON을 카카오 도서 검색 응답 DTO로 복원한다
            return objectMapper.readValue(cachedJson, KakaoBookJsonDto.class);
        }

        // 손상된 캐시나 Redis 장애는 외부 호출 예약 단계가 처리하도록 캐시 누락으로 전환한다
        catch (RuntimeException | JsonProcessingException e) {
            // 검색어 원문 없이 공용 캐시 조회 실패 원인을 기록한다
            log.error("카카오 도서 검색 공용 캐시를 조회하지 못했습니다.", e);
            // 복원할 수 없는 공용 검색 캐시를 사용하지 않는다
            return null;
        }
    }

    /**
     * 카카오 도서 검색 결과를 사용자와 연결하지 않은 공용 Redis 캐시에 저장한다
     *
     * @author SeungHyeon.Kang
     * @param query 사용자가 입력한 도서 검색어
     * @param page 카카오 도서 검색 페이지 번호
     * @param searchResult 카카오에서 받은 도서 검색 결과
     */
    public void setCachedSearch(String query, int page, KakaoBookJsonDto searchResult) {
        // 캐시 실패가 이미 완료된 카카오 검색 응답을 사용자에게 반환하지 못하게 하지 않도록 격리한다
        try {
            // 카카오 도서 검색 응답을 Redis 저장 문자열로 직렬화한다
            String cachedJson = objectMapper.writeValueAsString(searchResult);
            // 사용자 식별값 없는 공용 검색 결과를 설정된 짧은 유효시간 동안 저장한다
            redisTemplate.opsForValue().set(
                    getCacheKey(query, page)
                  , cachedJson
                  , Duration.ofSeconds(cacheTtlSeconds)
            );
        }

        // 공용 캐시 저장 실패는 쿼터 보호 카운터를 되돌리지 않고 운영 로그에만 기록한다
        catch (RuntimeException | JsonProcessingException e) {
            // 검색어 원문 없이 공용 캐시 저장 실패 원인을 기록한다
            log.error("카카오 도서 검색 공용 캐시를 저장하지 못했습니다.", e);
        }
    }

    /**
     * 물리 삭제 회원에게 남은 분간 및 일간 도서 검색 제한 데이터를 제거한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 물리 삭제가 완료된 회원 번호
     */
    public void delUserLimits(Long userNumb) {
        // 삭제할 회원 식별값이 없으면 다른 Redis 키에 영향을 주지 않고 종료한다
        if (StringUtil.isEmpty(userNumb)) {
            // 삭제 대상이 없는 검색 제한 정리를 종료한다
            return;
        }

        // 회원과 연결된 고정 이름의 분간 및 일간 제한 키를 함께 삭제한다
        redisTemplate.delete(List.of(getMinuteLimitKey(userNumb), getDailyLimitKey(userNumb)));
    }

    /**
     * 회원별 분간 도서 검색 제한 Redis 키를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 도서 검색을 요청한 회원 번호
     * @return 회원별 분간 제한 Redis 키
     */
    private String getMinuteLimitKey(Long userNumb) {
        // 회원 번호와 분간 제한 접두사를 결합한 Redis 키를 반환한다
        return MINUTE_LIMIT_KEY_PREFIX + userNumb;
    }

    /**
     * 회원별 일간 도서 검색 제한 Redis 키를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 도서 검색을 요청한 회원 번호
     * @return 회원별 일간 제한 Redis 키
     */
    private String getDailyLimitKey(Long userNumb) {
        // 회원 번호와 일간 제한 접두사를 결합한 Redis 키를 반환한다
        return DAILY_LIMIT_KEY_PREFIX + userNumb;
    }

    /**
     * 검색어 원문 대신 SHA-256 해시와 페이지를 사용하는 공용 캐시 키를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param query 사용자가 입력한 도서 검색어
     * @param page 카카오 도서 검색 페이지 번호
     * @return 검색어 원문을 포함하지 않는 공용 캐시 Redis 키
     */
    private String getCacheKey(String query, int page) {
        // 대소문자와 연속 공백 차이로 같은 검색이 중복 저장되지 않도록 검색어를 정규화한다
        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");

        // 런타임이 항상 제공하는 SHA-256 알고리즘으로 검색어 원문을 단방향 변환한다
        try {
            // 검색어와 페이지를 함께 해시할 메시지 다이제스트 객체를 생성한다
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            // 같은 검색어의 페이지별 캐시가 충돌하지 않도록 페이지를 해시 입력에 포함한다
            byte[] digest = messageDigest.digest((normalizedQuery + ":" + page).getBytes(StandardCharsets.UTF_8));
            // 검색어 원문이 없는 16진수 해시 Redis 키를 반환한다
            return SEARCH_CACHE_KEY_PREFIX + HexFormat.of().formatHex(digest);
        }

        // 필수 해시 알고리즘이 없는 런타임은 검색어 원문 캐시로 우회하지 않고 중단한다
        catch (NoSuchAlgorithmException e) {
            // SHA-256을 사용할 수 없는 실행 환경 오류를 호출부에 전달한다
            throw new IllegalStateException("SHA-256 algorithm is unavailable.", e);
        }
    }
}
