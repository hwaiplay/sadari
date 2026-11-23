package org.our.sadari.report.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.sql.Connection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.feed.dto.FeedDto;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.report.dto.ReportDto;
import org.our.sadari.report.dto.ReportTranslationDto;
import org.our.sadari.report.mapper.ReportMapper;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.time.Duration;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.client.RestTemplate;

/**
 * fileName       : ReportTranslationServiceTest
 * author         : HanWon.Jang
 * date           : 2026-09-07
 * description    : 독후감 번역 캐시 재사용과 월간 버튼 노출 제한을 검증
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-07        HanWon.Jang        최초 생성
 * 2026-09-08        HanWon.Jang        한영 본문 번역 방향 검증
 * 2026-11-23        SeungHyeon.Kang    외부 번역 대기 중 트랜잭션 검증
 */
@ExtendWith(MockitoExtension.class)
class ReportTranslationServiceTest {

    /**
     * 번역 대기 중 본문 변경과 접근 제한을 저장 전에 다시 검사하는지 검증
     *
     * @author SeungHyeon.Kang
     * @param modified 본문 수정 여부이며 false이면 접근 제한 전환
     */
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void rejectChangedTranslation(boolean modified) {
        // 번역 시작 시점의 원문 생성
        ReportTranslationDto original = getSourceFixture();
        // 번역 완료 시점의 변경된 원문 생성
        ReportTranslationDto changed = getSourceFixture();
        // 처음 번역한 원문과 다른 본문 설정
        changed.setSourceContent("수정된 본문");
        // 사전 조회·잠금 후 조회·최종 저장 시점의 상태 변화 설정
        when(reportMapper.getReportTrnsSource(any(ReportTranslationDto.class)))
                .thenReturn(original, original, modified ? changed : null);
        // 잠금 획득에 사용할 문자열 연산 대역 설정
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // 잠금 획득 성공 설정
        when(valueOperations.setIfAbsent(any(String.class), any(String.class), any(Duration.class))).thenReturn(true);
        // 월간 사용량 예약과 잠금 정리의 명시적 성공 설정
        when(redisTemplate.execute(org.mockito.ArgumentMatchers.<RedisScript<Long>>any(), anyList(), any(Object[].class)))
                .thenReturn(1L);
        // 실제 Google 호출 없이 번역 응답 설정
        when(restTemplate.postForObject(any(String.class), any(), eq(Map.class)))
                .thenReturn(Map.of("data", Map.of("translations", List.of(Map.of("translatedText", "Old content translation")))));
        // 원문 변경과 접근 제한이 경합하는 번역 실행
        ResultData result = translationService.setReportTranslation(31L, 17L);
        // 수정된 원문은 재시도 안내로, 접근 제한은 권한 거절로 확정
        assertEquals((modified ? ResultEnum.REPORT_TRANSLATION_FAILED : ResultEnum.FORBIDDEN).getCode(), result.getCode());
        // 이전 원문의 번역 저장 차단 확인
        verify(reportMapper, never()).setReportTrns(any(ReportTranslationDto.class));
    }

    /**
     * 실제 격리 Redis에서 동시 번역 공유와 잠금 만료·소유권 정리 검증
     *
     * @author SeungHyeon.Kang
     * @throws Exception 격리 Redis 또는 동시 실행 실패
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "SADARI_TEST_REDIS_PORT", matches = "\\d+")
    void shareWithRedisLease() throws Exception {
        // 포트만 잘못 지정하여 기존 Redis 데이터에 접근하지 않도록 명시적 임시 프로세스 식별값 요구
        String expectedPid = System.getenv("SADARI_TEST_REDIS_PID");
        // 임시 Redis 프로세스가 없는 일반 테스트 실행은 통합 테스트만 생략
        org.junit.jupiter.api.Assumptions.assumeTrue(!org.our.sadari.global.common.util.StringUtil.isEmpty(expectedPid));
        // 테스트 실행기가 따로 띄운 루프백 Redis만 연결
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("127.0.0.1", Integer.parseInt(System.getenv("SADARI_TEST_REDIS_PORT")));
        // 운영 Redis 설정을 사용하지 않을 연결 팩터리 생성
        LettuceConnectionFactory connections = new LettuceConnectionFactory(config);
        // 실제 Redis 연결 초기화
        connections.afterPropertiesSet();
        // 격리 Redis 전용 문자열 연산 객체 생성
        StringRedisTemplate redis = new StringRedisTemplate(connections);
        // 서비스가 테스트 Redis만 사용하도록 의존성 교체
        ReflectionTestUtils.setField(translationService, "redisTemplate", redis);
        // 실제 캐시 저장 결과를 대신할 메모리 상태 생성
        AtomicReference<ReportTranslationDto> cache = new AtomicReference<>();
        // 외부 번역 요청 횟수 확인용 카운터 생성
        AtomicInteger calls = new AtomicInteger();
        // Google 응답 시작 확인용 신호 생성
        CountDownLatch started = new CountDownLatch(1);
        // 두 요청이 원문 조회까지 도달했는지 확인할 신호 생성
        CountDownLatch sources = new CountDownLatch(3);
        // 공급자 대역의 응답 완료 제어
        CountDownLatch release = new CountDownLatch(1);
        // 두 사용자의 번역 요청 실행기 생성
        ExecutorService executor = Executors.newFixedThreadPool(2);
        // 잠금과 연결 및 실행기의 정리 보장
        try {
            // 데이터 쓰기 전에 실제 임시 Redis 프로세스 확인
            java.util.Properties info = redis.execute((org.springframework.data.redis.core.RedisCallback<java.util.Properties>)
                    connection -> connection.serverCommands().info("server"));
            // 테스트 Redis 서버 정보 확인
            assertNotNull(info);
            // 기존 서비스 Redis에 잘못 연결한 경우 모든 쓰기 작업 차단
            assertEquals(expectedPid, info.getProperty("process_id"));
            // 이번 검증의 월간 사용량 증가분만 비교할 실제 사용량 키 조회
            String usageKey = ReflectionTestUtils.invokeMethod(translationService, "getMonthlyUsageKey");
            // 이전 검증의 독립된 사용량 보존
            String storedUsage = redis.opsForValue().get(usageKey);
            // 최초 실행의 빈 사용량은 영으로 계산
            long usedBefore = org.our.sadari.global.common.util.StringUtil.isEmpty(storedUsage) ? 0L : Long.parseLong(storedUsage);
            // 각 조회는 운영 DB처럼 독립된 원문 DTO 반환
            when(reportMapper.getReportTrnsSource(any(ReportTranslationDto.class))).thenAnswer(invocation -> {
                // 두 요청이 번역 전 조회에 진입한 사실 기록
                sources.countDown();
                // 독립된 테스트 원문 스냅샷 반환
                return getSourceFixture();
            });
            // 먼저 저장된 번역 결과를 후속 요청에 재사용
            when(reportMapper.getReportTrnsDtl(any(ReportTranslationDto.class))).thenAnswer(invocation -> cache.get());
            // 번역 캐시 저장 대역 구성
            when(reportMapper.setReportTrns(any(ReportTranslationDto.class))).thenAnswer(invocation -> {
                // 저장할 번역 결과 보관
                cache.set(invocation.getArgument(0));
                // 한 건의 캐시 저장 결과 반환
                return 1;
            });
            // 실제 외부 비용 없이 번역 대기와 실행 횟수 검증
            when(restTemplate.postForObject(any(String.class), any(), eq(Map.class))).thenAnswer(invocation -> {
                // 외부 호출 횟수 기록
                calls.incrementAndGet();
                // 공급자 호출 시작 전달
                started.countDown();
                // 두 번째 요청의 합류 확인까지 응답 보류
                assertTrue(release.await(5, TimeUnit.SECONDS));
                // 테스트 번역 응답 반환
                return Map.of("data", Map.of("translations", List.of(Map.of("translatedText", "Shared translation"))));
            });
            // 동일 원문의 첫 번째 번역 실행
            Future<ResultData> first = executor.submit(this::getFixtureTranslation);
            // 첫 번째 번역의 외부 호출 진입 확인
            assertTrue(started.await(5, TimeUnit.SECONDS));
            // 동일 원문의 두 번째 번역 실행
            Future<ResultData> second = executor.submit(this::getFixtureTranslation);
            // 두 요청 모두 원문 조회까지 진입한 사실 확인
            assertTrue(sources.await(5, TimeUnit.SECONDS));
            // 외부 응답 전에 중복 번역이 없는지 확인
            assertEquals(1, calls.get());
            // 실제 Redis 잠금으로 대기 중인 두 번째 요청 확인
            assertFalse(second.isDone());
            // 최초 외부 응답 완료 허용
            release.countDown();
            // 최초 번역의 정상 저장 확인
            assertEquals(200, first.get(5, TimeUnit.SECONDS).getCode());
            // 대기 요청의 저장된 캐시 재사용 확인
            assertEquals(200, second.get(5, TimeUnit.SECONDS).getCode());
            // 실제 공급자 호출이 한 번만 발생한 사실 확인
            assertEquals(1, calls.get());
            // 한 번의 캐시 저장만 발생한 사실 확인
            verify(reportMapper).setReportTrns(any(ReportTranslationDto.class));
            // 두 요청이 실제 Redis 월간 한도도 한 번만 예약한 사실 확인
            assertEquals(getSourceFixture().getSourceContent().codePointCount(0, getSourceFixture().getSourceContent().length())
                       , Long.parseLong(redis.opsForValue().get(usageKey)) - usedBefore);
            // 소유권과 만료 검증 전용 잠금 키 구성
            String key = "test:translation:lease";
            // 이전 요청이 소유했던 잠금의 새 소유자 구성
            redis.opsForValue().set(key, "new-owner", Duration.ofSeconds(5));
            // 이전 요청의 늦은 정리가 새 잠금을 삭제하지 않도록 운영 Lua 실행
            ReflectionTestUtils.invokeMethod(translationService, "delTranslationLock", key, "old-owner");
            // 새 소유자의 잠금 유지 확인
            assertEquals("new-owner", redis.opsForValue().get(key));
            // 현재 소유자만 잠금 해제 가능
            ReflectionTestUtils.invokeMethod(translationService, "delTranslationLock", key, "new-owner");
            // 해제된 잠금의 부재 확인
            assertFalse(Boolean.TRUE.equals(redis.hasKey(key)));
            // 만료 재시도 검증에만 짧은 최대 대기시간 적용
            ReflectionTestUtils.setField(translationService, "lockSeconds", 1L);
            // 프로세스가 사라진 작업자의 만료 예정 잠금 생성
            redis.opsForValue().set(key, "expired-owner", Duration.ofMillis(100));
            // 만료 후 새 요청이 DB 연결 없이 잠금을 얻는지 확인
            assertEquals(Boolean.TRUE, ReflectionTestUtils.invokeMethod(translationService, "getTranslationLock", key, "next-owner"));
            // 검증한 잠금 정리
            ReflectionTestUtils.invokeMethod(translationService, "delTranslationLock", key, "next-owner");
        }

        // 실패 시에도 외부 대역과 Redis 연결 정리
        finally {
            // 응답 대기 해제
            release.countDown();
            // 요청 실행기 종료
            executor.shutdownNow();
            // 남은 작업의 종료 확인
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
            // 테스트 Redis 연결 반환
            connections.destroy();
        }

    }

    /**
     * 실행기 스레드에서 테스트 언어 설정과 정리를 포함한 번역 요청 수행
     *
     * @author SeungHyeon.Kang
     * @return 가상 독후감의 번역 결과
     */
    private ResultData getFixtureTranslation() {
        // 요청별 영어 번역 대상 설정
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        // 실행기 스레드에 남는 언어 상태 방지
        try {
            // 동일 원문의 가상 사용자 요청 결과 반환
            return translationService.setReportTranslation(31L, 17L);
        }

        // 다른 테스트로 언어가 전파되지 않도록 정리
        finally {
            // 요청 언어 초기화
            LocaleContextHolder.resetLocaleContext();
        }

    }

    /**
     * 외부 통신과 실제 회원 정보를 사용하지 않는 번역 원문 생성
     *
     * @author SeungHyeon.Kang
     * @return 가상 독후감 원문
     */
    private ReportTranslationDto getSourceFixture() {
        // 독립된 원문 DTO 생성
        ReportTranslationDto source = new ReportTranslationDto();
        // 가상 독후감 번호 설정
        source.setReptNumb(17L);
        // 원문 언어 설정
        source.setSourceLangCode("ko");
        // 외부로 보내지 않을 가상 원문 설정
        source.setSourceContent("동시 번역 테스트 본문");
        // 테스트 원문 반환
        return source;
    }

    /**
     * 외부 번역 응답 대기 전에 실제 Spring 트랜잭션과 JDBC 연결을 반환하는지 검증
     *
     * @author SeungHyeon.Kang
     * @throws Exception 격리된 트랜잭션 또는 비동기 테스트 실행 실패
     */
    @Test
    void translateReleasesTx() throws Exception {
        // 운영 DB에 연결하지 않을 JDBC 대역 생성
        DataSource dataSource = mock(DataSource.class);
        // 연결 반환 시점을 확인할 JDBC 연결 대역 생성
        Connection connection = mock(Connection.class);
        // Spring 트랜잭션이 사용할 단일 연결 설정
        when(dataSource.getConnection()).thenReturn(connection);
        // 실제 트랜잭션 시작 시 자동 커밋 해제 조건 설정
        when(connection.getAutoCommit()).thenReturn(true);
        // 운영 Transactional 어노테이션을 해석할 인터셉터 생성
        TransactionInterceptor interceptor = new TransactionInterceptor();
        // 실제 Spring JDBC 트랜잭션 관리 적용
        DataSourceTransactionManager jdbcTransactions = new DataSourceTransactionManager(dataSource);
        // 내부의 짧은 조회·저장 경계도 실제 JDBC 트랜잭션으로 검증
        ReflectionTestUtils.setField(translationService, "transactionManager", jdbcTransactions);
        // 호출부 경계에도 같은 실제 트랜잭션 관리자 적용
        interceptor.setTransactionManager(jdbcTransactions);
        // 운영 메서드 어노테이션의 트랜잭션 경계 적용
        interceptor.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        // 운영 서비스 호출과 같은 AOP 경계 생성
        ProxyFactory factory = new ProxyFactory(translationService);
        // 외부 번역 호출 전후의 실제 트랜잭션 처리 연결
        factory.addAdvice(interceptor);
        // 어노테이션 기반 트랜잭션이 적용된 서비스 프록시 생성
        ReportTranslationService service = (ReportTranslationService) factory.getProxy();
        // 공개 범위 확인을 통과할 가상 독후감 생성
        ReportTranslationDto source = new ReportTranslationDto();
        // 가상 독후감 식별값 설정
        source.setReptNumb(17L);
        // 영어 번역이 필요한 원문 언어 설정
        source.setSourceLangCode("ko");
        // 외부 공급자에 전송하지 않을 테스트 본문 설정
        source.setSourceContent("트랜잭션 테스트 본문");
        // 실제 DB 조회 대신 원문 잠금 조회의 결과 설정
        when(reportMapper.getReportTrnsSource(any(ReportTranslationDto.class))).thenReturn(source);
        // 캐시 조회도 현재 원문 권한 확인과 같은 트랜잭션에서 수행하는지 검증
        when(reportMapper.getReportTrnsDtl(any(ReportTranslationDto.class))).thenAnswer(invocation -> {
            // 캐시와 원문 사이의 접근 상태 변경 경합 방지 확인
            assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
            // 신규 번역이 필요한 미적중 결과 반환
            return null;
        });
        // 외부 번역 허용량 예약 성공 설정
        when(redisTemplate.execute(org.mockito.ArgumentMatchers.<RedisScript<Long>>any(), anyList(), any(Object[].class)))
                .thenReturn(1L);
        // 실제 Redis 없이 잠금 획득 성공 설정
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // 잠금 대기 없이 신규 번역 수행
        when(valueOperations.setIfAbsent(any(String.class), any(String.class), any(java.time.Duration.class))).thenReturn(true);
        // 외부 호출 진입을 확인할 신호 생성
        CountDownLatch started = new CountDownLatch(1);
        // 임의 시간 측정 대신 응답 완료 시점을 제어할 신호 생성
        CountDownLatch release = new CountDownLatch(1);
        // Google API 호출 대역에서 실제 트랜잭션 활성 여부 검증
        when(restTemplate.postForObject(eq("https://example.invalid/translate"), any(), eq(Map.class)))
                .thenAnswer(invocation -> {
                    // 외부 응답 대기 시점에는 DB 트랜잭션이 없는지 확인
                    assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
                    // 사전 확인과 잠금 획득 후 재확인이 외부 호출보다 먼저 실행된 사실 확인
                    verify(reportMapper, times(2)).getReportTrnsSource(any(ReportTranslationDto.class));
                    // 외부 호출 진입을 테스트 스레드에 전달
                    started.countDown();
                    // 테스트가 응답을 허용하기 전까지 외부 호출 대기 재현
                    assertTrue(release.await(5, TimeUnit.SECONDS));
                    // 가상 공급자의 정상 번역 응답 반환
                    return Map.of("data", Map.of("translations", List.of(Map.of("translatedText", "Test translation"))));
                });
        // HTTP 요청 처리 스레드를 대신할 실행기 생성
        ExecutorService executor = Executors.newSingleThreadExecutor();
        // 응답 신호와 실행기를 실패 경로에서도 정리할 경계
        try {
            // 실제 프록시를 통해 번역 요청 실행
            Future<ResultData> result = executor.submit(() -> {
                // 요청 스레드의 표시 언어 설정
                LocaleContextHolder.setLocale(Locale.ENGLISH);
                // 비동기 요청의 언어 컨텍스트 정리 보장
                try {
                    // 실제 서비스의 트랜잭션 경계를 거친 번역 결과 반환
                    return service.setReportTranslation(31L, 17L);
                }

                // 실행기 스레드에 남을 요청 언어 제거
                finally {
                    // 다음 요청으로 언어 상태가 전파되지 않도록 초기화
                    LocaleContextHolder.resetLocaleContext();
                }

            });
            // 가상 외부 API 응답 대기 시점 도달 확인
            assertTrue(started.await(5, TimeUnit.SECONDS));
            // 외부 응답이 오기 전 사용자 요청 미완료 확인
            assertFalse(result.isDone());
            // 사전 조회 두 건이 외부 응답 대기 전에 커밋된 상태 확인
            verify(connection, times(2)).commit();
            // 외부 응답 대기 전에 JDBC 연결이 모두 반환된 상태 확인
            verify(connection, times(2)).close();
            // 외부 번역 응답 허용
            release.countDown();
            // 응답 수신 뒤 정상 번역 완료 확인
            assertEquals(200, result.get(5, TimeUnit.SECONDS).getCode());
            // 외부 응답 뒤 재검증과 저장의 짧은 추가 커밋 확인
            verify(connection, times(3)).commit();
            // 완료된 모든 JDBC 연결 반환 확인
            verify(connection, times(3)).close();
        }

        // 실패 시에도 대기 중인 요청과 실행기 정리
        finally {
            // 외부 API 대역의 대기 해제
            release.countDown();
            // 테스트 실행기 종료
            executor.shutdownNow();
            // 테스트 이후 비동기 작업이 남지 않도록 종료 확인
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }

    }

    // 독후감 원문과 번역 캐시 데이터 접근 객체
    @Mock
    private ReportMapper reportMapper;
    // 월간 번역 사용량 저장 객체
    @Mock
    private StringRedisTemplate redisTemplate;
    // Redis 문자열 값 연산 객체
    @Mock
    private ValueOperations<String, String> valueOperations;
    // Google 번역 API 호출 객체
    @Mock
    private RestTemplate restTemplate;
    // DB 실행 없이 짧은 트랜잭션 경계만 대체할 관리자
    @Mock
    private PlatformTransactionManager transactionManager;
    // 단위 테스트용 트랜잭션 상태
    @Mock
    private TransactionStatus transactionStatus;

    // 독후감 번역 단위 테스트 대상
    private ReportTranslationService translationService;

    /**
     * 각 테스트에서 영어 표시 언어와 동일한 번역 설정을 구성
     *
     * @author HanWon.Jang
     */
    @BeforeEach
    void setUp() {
        // 거절 경로의 실제 사용자 메시지 프로퍼티 구성
        org.springframework.context.support.ResourceBundleMessageSource messages = new org.springframework.context.support.ResourceBundleMessageSource();
        // 운영과 같은 공통 메시지 원본 지정
        messages.setBasename("messages");
        // 테스트 한글 메시지 인코딩 보존
        messages.setDefaultEncoding("UTF-8");
        // 실패 응답의 메시지 조회 경로 초기화
        new org.our.sadari.global.common.util.MessageUtils().setMessageSource(messages);
        // 번역 서비스에 독립된 Mock 의존성을 주입
        translationService = new ReportTranslationService(reportMapper, redisTemplate, restTemplate, transactionManager);
        // 조회와 저장 단위 테스트에서 독립된 트랜잭션 상태 반환
        lenient().when(transactionManager.getTransaction(any(TransactionDefinition.class))).thenReturn(transactionStatus);
        // 단위 테스트에서 외부 호출 여부만 판정할 수 있도록 API 설정을 주입
        ReflectionTestUtils.setField(translationService, "translationUrl", "https://example.invalid/translate");
        ReflectionTestUtils.setField(translationService, "translationApiKey", "test-key");
        ReflectionTestUtils.setField(translationService, "monthlyCharacterLimit", 500000L);
        ReflectionTestUtils.setField(translationService, "quotaZone", "America/Los_Angeles");
        // 운영 기본값과 같은 잠금 유효시간 설정
        ReflectionTestUtils.setField(translationService, "lockSeconds", 30L);
        // 한국어 원문을 영어로 표시하는 요청 환경을 구성
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    /**
     * 테스트별 요청 언어가 다른 테스트로 전파되지 않도록 초기화
     *
     * @author HanWon.Jang
     */
    @AfterEach
    void tearDown() {
        // 스레드에 저장된 테스트 언어 환경을 제거
        LocaleContextHolder.resetLocaleContext();
    }

    /**
     * 유효한 번역 캐시는 Redis 예약과 Google 호출 없이 반환하는지 검증
     *
     * @author HanWon.Jang
     */
    @ParameterizedTest
    @CsvSource({"en,ko,좋은 책입니다,This is a good book.", "ko,ko,This is a good book.,좋은 책입니다"})
    void reuseTranslationCache(String targetLanguage, String storedLanguage, String content
                              , String translatedContent) {
        // 작성 당시 앱 언어와 본문이 다른 경우에도 현재 표시 언어로 번역 요청
        LocaleContextHolder.setLocale(Locale.forLanguageTag(targetLanguage));
        // 공개 범위 검증을 통과할 한국어 독후감 원문을 구성
        ReportTranslationDto source = new ReportTranslationDto();
        source.setReptNumb(17L);
        source.setSourceLangCode(storedLanguage);
        source.setSourceContent(content);
        when(reportMapper.getReportTrnsSource(any(ReportTranslationDto.class))).thenReturn(source);
        // 서비스가 계산한 원문 해시와 같은 유효 캐시를 동적으로 반환
        when(reportMapper.getReportTrnsDtl(any(ReportTranslationDto.class))).thenAnswer(invocation -> {
            ReportTranslationDto request = invocation.getArgument(0);
            ReportTranslationDto cached = new ReportTranslationDto();
            cached.setReptNumb(request.getReptNumb());
            cached.setLangCode(request.getLangCode());
            cached.setOrigHash(request.getOrigHash());
            cached.setTrnsCntn(translatedContent);
            return cached;
        });

        // 같은 원문과 대상 언어의 번역을 요청
        ResultData result = translationService.setReportTranslation(31L, 17L);
        // 유효 캐시가 성공 응답으로 반환됐는지 확인
        assertEquals(200, result.getCode());
        ReportTranslationDto translation = (ReportTranslationDto) result.getData();
        assertTrue(translation.isCached());
        assertEquals(translatedContent, translation.getTrnsCntn());
        // 번역 캐시 조회 대상이 앱의 현재 표시 언어와 일치하는지 검증
        assertEquals(targetLanguage, translation.getLangCode());
        // 캐시 적중 요청은 월간 사용량과 Google 호출을 사용하지 않는지 확인
        verifyNoInteractions(redisTemplate, restTemplate);
    }

    /**
     * 작성 당시 앱 언어와 무관하게 목록과 피드의 본문 기준 번역 버튼 판정 검증
     *
     * @author HanWon.Jang
     * @param targetLanguage 현재 표시 언어
     * @param storedLanguage 작성 당시 언어
     * @param content 독후감 본문
     * @param expected 번역 버튼 표시 여부
     */
    @ParameterizedTest
    @CsvSource({
        "ko,ko,This is a good book.,Y",
        "ko,en,This is a good book.,Y",
        "en,en,정말 재미있게 읽은 책입니다,Y",
        "ko,en,정말 재미있게 읽은 책입니다,N",
        "en,ko,This is a good book.,N",
        "ko,ko,'',N",
        "ko,ko,123 😀,N"
    })
    void showByContentLanguage(String targetLanguage, String storedLanguage, String content
                               , String expected) {
        // 새 번역이 가능한 월간 잔여량과 표시 언어 구성
        LocaleContextHolder.setLocale(Locale.forLanguageTag(targetLanguage));
        // 월간 사용량 저장소 대체
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // 사용량이 없는 월의 번역 허용 상태 구성
        when(valueOperations.get(any(String.class))).thenReturn("0");
        // 공개 목록의 원문과 저장된 언어 구성
        ReportDto report = new ReportDto();
        // 작성 당시 앱 언어 설정
        report.setLangCode(storedLanguage);
        // 실제 작성한 본문 설정
        report.setReptCntn(content);
        // 동일한 독후감의 피드 항목 구성
        FeedDto feed = new FeedDto();
        // 번역 대상 독후감 피드 유형 설정
        feed.setTagtType(Constant.LIKE_TARGET_REPORT);
        // 작성 당시 앱 언어 설정
        feed.setLangCode(storedLanguage);
        // 실제 작성한 본문 설정
        feed.setReptCntn(content);
        // 공개 목록의 번역 버튼 판정 실행
        translationService.applyReportAvailability(List.of(report));
        // 피드의 번역 버튼 판정 실행
        translationService.applyFeedAvailability(List.of(feed));
        // 두 화면에 같은 본문 언어 판정 적용 여부 확인
        assertEquals(expected, report.getTrnsAvaiYsno());
        // 피드의 번역 버튼 표시 여부 확인
        assertEquals(expected, feed.getTrnsAvaiYsno());
    }

    /**
     * 월간 잔여량이 없는 새 번역 카드의 버튼을 숨기는지 검증
     *
     * @author HanWon.Jang
     */
    @Test
    void hideAtMonthlyLimit() {
        // 월간 500,000자를 이미 모두 사용한 Redis 상태를 구성
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any(String.class))).thenReturn("500000");
        // 캐시가 없는 한국어 공개 독후감 카드를 구성
        ReportDto report = new ReportDto();
        report.setLangCode("ko");
        report.setReptCntn("번역할 내용");
        report.setTrnsCacheYsno(Constant.COMM_NO);

        // 영어 표시 요청에 월간 사용량 정책을 적용
        translationService.applyReportAvailability(List.of(report));
        // 새 번역으로 한도를 넘기는 카드의 번역 버튼이 숨겨지는지 확인
        assertEquals(Constant.COMM_NO, report.getTrnsAvaiYsno());
    }

    /**
     * API 키가 없어도 유효한 번역 캐시는 계속 볼 수 있는지 검증
     *
     * @author HanWon.Jang
     */
    @Test
    void showCacheWithoutApiKey() {
        // 신규 Google 호출을 할 수 없는 API 키 누락 상태를 구성
        ReflectionTestUtils.setField(translationService, "translationApiKey", "");
        // 현재 원문과 대상 언어에 해당하는 유효 캐시가 있는 카드를 구성
        ReportDto report = new ReportDto();
        report.setLangCode("ko");
        report.setReptCntn("이미 번역된 내용");
        report.setTrnsCacheYsno(Constant.COMM_YES);

        // 영어 표시 요청에 캐시 우선 노출 정책을 적용
        translationService.applyReportAvailability(List.of(report));
        // API 키와 월간 잔여량에 관계없이 캐시 보기 버튼이 유지되는지 확인
        assertEquals(Constant.COMM_YES, report.getTrnsAvaiYsno());
        verifyNoInteractions(redisTemplate, restTemplate);
    }
}
