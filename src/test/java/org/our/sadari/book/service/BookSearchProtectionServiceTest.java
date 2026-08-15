package org.our.sadari.book.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.book.dto.KakaoBookJsonDto;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * fileName       : BookSearchProtectionServiceTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-16
 * description    : Redis 도서 검색 제한과 검색어 비노출 공용 캐시를 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-16        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class BookSearchProtectionServiceTest {

    // 도서 검색 제한과 캐시를 저장할 Redis 문자열 연산 대역
    @Mock
    private StringRedisTemplate redisTemplate;
    // 공용 검색 결과 Redis 값 연산 대역
    @Mock
    private ValueOperations<String, String> valueOperations;
    // 검색어 원문 비노출 캐시 키 검증 객체
    @Captor
    private ArgumentCaptor<String> cacheKeyCaptor;

    // Redis 도서 검색 보호 검증 대상 서비스
    private BookSearchProtectionService bookSearchProtectionService;

    /**
     * 각 테스트에서 도서 검색 보호 서비스와 운영 기본 제한값을 구성한다
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // 실제 JSON 계약으로 캐시 직렬화를 확인할 객체를 생성한다
        ObjectMapper objectMapper = new ObjectMapper();
        // Redis 대역과 JSON 객체로 도서 검색 보호 검증 대상을 생성한다
        bookSearchProtectionService = new BookSearchProtectionService(redisTemplate, objectMapper);
        // 회원별 60초 요청 제한 기본값을 설정한다
        ReflectionTestUtils.setField(bookSearchProtectionService, "rateLimitPerMinute", 20);
        // 회원별 24시간 요청 제한 기본값을 설정한다
        ReflectionTestUtils.setField(bookSearchProtectionService, "rateLimitPerDay", 200);
        // 앱 전체 카카오 실제 호출 보호 기본값을 설정한다
        ReflectionTestUtils.setField(bookSearchProtectionService, "providerCallLimitPerDay", 27000);
        // 공용 검색 결과 캐시 기본 유효시간을 설정한다
        ReflectionTestUtils.setField(bookSearchProtectionService, "cacheTtlSeconds", 600);
    }

    /**
     * 회원별 두 제한을 하나의 Redis Lua 실행으로 확인하고 허용 결과를 반환하는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void allowsRequestWithinMemberLimits() {
        // Redis Lua Script가 회원의 요청을 허용하도록 설정한다
        when(redisTemplate.execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any()
              , eq(List.of("book:search:rate:minute:7", "book:search:rate:day:7"))
              , eq("20"), eq("200"), eq("60"), eq("86400")
        )).thenReturn(1L);

        // 로그인 회원의 현재 도서 검색 요청 허용 여부를 확인한다
        boolean allowed = bookSearchProtectionService.isRequestAllowed(7L);

        // 분간 및 일간 제한 안의 요청이 허용되는지 확인한다
        assertTrue(allowed);
    }

    /**
     * Redis 장애 시 카카오 쿼터 보호를 우선해 회원 검색을 차단하는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void blocksRequestWhenRedisFails() {
        // 회원 제한 Lua 실행 단계에서 Redis 장애가 발생하도록 설정한다
        when(redisTemplate.execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any()
              , eq(List.of("book:search:rate:minute:7", "book:search:rate:day:7"))
              , eq("20"), eq("200"), eq("60"), eq("86400")
        )).thenThrow(new IllegalStateException("Redis unavailable"));

        // Redis 장애 중 로그인 회원의 검색 허용 여부를 확인한다
        boolean allowed = bookSearchProtectionService.isRequestAllowed(7L);

        // 제한을 확인할 수 없는 요청이 차단되는지 확인한다
        assertFalse(allowed);
    }

    /**
     * 공용 검색 결과 캐시 키가 검색어 원문을 포함하지 않고 설정 TTL로 저장되는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void cachesSearchWithoutPlainQuery() {
        // Redis 값 저장 연산을 공용 검색 캐시에 사용할 수 있도록 설정한다
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // 마지막 페이지인 빈 카카오 검색 응답을 생성한다
        KakaoBookJsonDto searchResult = new KakaoBookJsonDto();
        // 캐시 직렬화 대상에 빈 도서 목록을 설정한다
        searchResult.setDocuments(List.of());

        // 검색어와 첫 페이지의 카카오 응답을 공용 캐시에 저장한다
        bookSearchProtectionService.setCachedSearch("민감한 검색어", 1, searchResult);

        // 공용 캐시 키와 JSON 및 TTL이 Redis에 저장되었는지 확인한다
        verify(valueOperations).set(cacheKeyCaptor.capture(), anyString(), eq(Duration.ofSeconds(600)));
        // Redis 키가 도서 검색 공용 캐시 영역을 사용하는지 확인한다
        assertTrue(cacheKeyCaptor.getValue().startsWith("book:search:cache:"));
        // Redis 키에 사용자가 입력한 검색어 원문이 포함되지 않았는지 확인한다
        assertFalse(cacheKeyCaptor.getValue().contains("민감한 검색어"));
    }

    /**
     * 물리 삭제 회원의 고정 이름 분간 및 일간 제한 키를 함께 삭제하는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void deletesMemberRateLimits() {
        // 물리 삭제된 회원의 도서 검색 제한 데이터를 정리한다
        bookSearchProtectionService.delUserLimits(7L);

        // 분간 및 일간 제한 키가 한 번의 Redis 삭제로 제거되는지 확인한다
        verify(redisTemplate).delete(List.of("book:search:rate:minute:7", "book:search:rate:day:7"));
    }
}
