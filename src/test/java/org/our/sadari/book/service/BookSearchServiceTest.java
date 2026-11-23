package org.our.sadari.book.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import com.sun.net.httpserver.HttpServer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.book.dto.BookJsonDto;
import org.our.sadari.book.dto.BookSearchResponseDto;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.MessageUtils;
import org.our.sadari.user.dto.UserSettingDto;
import org.our.sadari.user.mapper.UserMapper;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * fileName       : BookSearchServiceTest
 * author         : HanWon.Jang
 * date           : 2026-07-31
 * description    : 계정 언어별 카카오와 Google Books 검색 및 화면 응답 변환을 검증
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-31        SeungHyeon.Kang    최초 생성
 * 2026-08-16        SeungHyeon.Kang    도서 검색·인기 검색어 검증 추가
 * 2026-08-28        HanWon.Jang        캐시 유형별 단기 한도 검증
 * 2026-09-08        HanWon.Jang        영어 설정 Google Books 검색 검증 추가
 * 2026-11-23        SeungHyeon.Kang    동시 캐시 미적중 중복 HTTP 호출 검증
 */
@ExtendWith(MockitoExtension.class)
class BookSearchServiceTest {

    /**
     * 최초 공유 검색이 쿼터 또는 공급자 오류로 실패해도 다음 요청에서 정상 재시도 가능한지 검증
     *
     * @author SeungHyeon.Kang
     * @param limited 최초 실패가 쿼터 제한인지 여부
     */
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void retryFailedSharedSearch(boolean limited) {
        // 한국어 검색 공급자를 선택할 계정 설정
        when(userMapper.getUserSettingDtl(7L)).thenReturn(getSetting("N"));
        // 각 요청의 독립된 단기 한도 허용
        when(bookSearchProtectionService.isRequestAllowed(7L, false)).thenReturn(true);
        // 첫 예약만 거절하거나 두 예약 모두 허용하는 상태 설정
        when(bookSearchProtectionService.reserveProviderCall(7L, "kakao")).thenReturn(!limited, true);
        // 공급자 실제 호출 횟수를 세는 대역 생성
        AtomicInteger calls = new AtomicInteger();
        // 공급자 장애와 복구를 외부 호출 없이 재현
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class))).thenAnswer(invocation -> {
            // 실제 공급자 호출 횟수 기록
            int attempt = calls.incrementAndGet();
            // 한도 제한이 아닌 경우 첫 번째 외부 호출만 실패
            if (!limited && attempt == 1) {
                // 복구 가능한 가상 통신 실패 생성
                throw new org.springframework.web.client.ResourceAccessException("Test provider unavailable");
            }

            // 복구된 공급자의 정상 빈 응답 반환
            return ResponseEntity.ok("{\"meta\":{\"is_end\":true},\"documents\":[]}");
        });
        // 최초 요청의 기존 업무 실패 응답 확인
        assertEquals((limited ? ResultEnum.BOOK_SEARCH_RATE_LIMITED : ResultEnum.COMMON_SEARCH_REJECTED).getCode()
                   , bookSearchService.searchBooks(7L, "재시도 테스트", 1).getCode());
        // 실패한 공유 작업이 남아 다음 요청을 막지 않는지 확인
        assertEquals(200, bookSearchService.searchBooks(7L, "재시도 테스트", 1).getCode());
        // 한도 거절에서는 실제 공급자 요청이 발생하지 않은 사실 확인
        assertEquals(limited ? 1 : 2, calls.get());
    }

    /**
     * 같은 검색어의 동시 캐시 미적중이 실제 HTTP 호출과 공급자 쿼터를 한 번만 사용하는지 검증
     *
     * @author SeungHyeon.Kang
     * @throws Exception 격리된 HTTP 서버 또는 동시 요청 실행 실패
     */
    @Test
    void shareColdSearchCalls() throws Exception {
        // 실제 외부 공급자 대신 사용할 메모리 캐시 생성
        AtomicReference<BookSearchResponseDto> cache = new AtomicReference<>();
        // 실제 HTTP 요청 개수를 확인할 카운터 생성
        AtomicInteger calls = new AtomicInteger();
        // 두 요청이 공급자까지 도달하는 시점을 확인할 신호 생성
        CountDownLatch entered = new CountDownLatch(1);
        // 두 요청이 진행 중 작업 조회까지 도달한 시점 확인
        CountDownLatch sharing = new CountDownLatch(2);
        // 첫 응답의 캐시 저장 전에 동시 미적중을 재현할 신호 생성
        CountDownLatch release = new CountDownLatch(1);
        // 운영 API에 접근하지 않는 루프백 임시 HTTP 서버 생성
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        // 동시 공급자 응답 처리를 위한 실행기 생성
        ExecutorService provider = Executors.newFixedThreadPool(2);
        // 공급자 대역도 두 요청을 병렬로 처리하도록 설정
        server.setExecutor(provider);
        // 실제 HTTP 응답으로 빈 검색 결과를 반환할 경로 등록
        server.createContext("/search", exchange -> {
            // 응답 완료 여부와 무관하게 소켓을 정리할 경계
            try {
                // 실제 공급자 HTTP 요청 횟수 증가
                calls.incrementAndGet();
                // 공급자 요청 도착 신호 전달
                entered.countDown();
                // 테스트가 허용할 때까지 공급자 응답 보류
                assertTrue(release.await(5, TimeUnit.SECONDS));
                // 캐시할 수 있는 정상 빈 검색 결과 생성
                byte[] body = "{\"meta\":{\"is_end\":true,\"pageable_count\":0,\"total_count\":0},\"documents\":[]}"
                        .getBytes(StandardCharsets.UTF_8);
                // JSON 응답 형식 지정
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                // 정상 HTTP 응답 시작
                exchange.sendResponseHeaders(200, body.length);
                // 실제 응답 본문 전송
                exchange.getResponseBody().write(body);
            }

            // 테스트 종료 중 인터럽트가 발생해도 실행기 종료 상태 보존
            catch (InterruptedException e) {
                // 현재 스레드의 인터럽트 복원
                Thread.currentThread().interrupt();
            }

            // 공급자 대역의 요청 소켓 정리
            finally {
                // 응답 성공과 실패에 관계없이 연결 종료
                exchange.close();
            }

        });
        // 테스트 중에만 사용할 HTTP 서버 시작
        server.start();
        // 두 사용자 검색 요청을 병렬 실행할 실행기 생성
        ExecutorService requests = Executors.newFixedThreadPool(2);
        // 서버와 작업 스레드를 실패 경로에서도 정리할 경계
        try {
            // 실제 HTTP 클라이언트를 사용하는 운영 검색 서비스 생성
            BookSearchService service = new BookSearchService(new RestTemplate(), objectMapper, bookSearchProtectionService, userMapper);
            // 운영 주소 대신 임시 루프백 서버 주소 생성
            URI endpoint = new URI("http", null, server.getAddress().getAddress().getHostAddress()
                                 , server.getAddress().getPort(), "/search", null, null);
            // 실제 HTTP 통신 대상의 임시 주소 설정
            ReflectionTestUtils.setField(service, "bookSearchUrl", endpoint.toString());
            // 실제 API 인증정보를 사용하지 않을 가상 키 설정
            ReflectionTestUtils.setField(service, "kakaoRestApiKey", "test-key");
            // 같은 한국어 공급자를 선택할 테스트 계정 설정
            when(userMapper.getUserSettingDtl(anyLong())).thenReturn(getSetting("N"));
            // 테스트 요청의 단기 한도 허용
            when(bookSearchProtectionService.isRequestAllowed(anyLong(), anyBoolean())).thenReturn(true);
            // 실제 과금 없이 공급자 예약 성공 설정
            when(bookSearchProtectionService.reserveProviderCall(anyLong(), eq("kakao"))).thenReturn(true);
            // 첫 응답 저장 전에는 미적중이고 저장 후에는 적중하는 캐시 구성
            when(bookSearchProtectionService.getCachedSearch("kakao", "동시 검색 테스트", 1))
                    .thenAnswer(invocation -> cache.get());
            // 실제 캐시 키 계산을 거친 두 요청의 합류 확인
            when(bookSearchProtectionService.getCacheKey("kakao", "동시 검색 테스트", 1)).thenAnswer(invocation -> {
                // 두 요청의 진행 중 작업 조회 시점 기록
                sharing.countDown();
                // 운영 캐시와 같은 해시 키 반환
                return invocation.callRealMethod();
            });
            // 운영 서비스가 저장한 실제 변환 결과를 다음 요청에 재사용
            doAnswer(invocation -> {
                // 검색 응답을 메모리 캐시에 저장
                cache.set(invocation.getArgument(3));
                // 캐시 저장 대역 완료
                return null;
            }).when(bookSearchProtectionService).setCachedSearch(eq("kakao"), eq("동시 검색 테스트"), eq(1), any(BookSearchResponseDto.class));
            // 동일 검색어의 첫 사용자 요청 실행
            Future<ResultData> first = requests.submit(() -> service.searchBooks(71L, "동시 검색 테스트", 1));
            // 동일 검색어의 두 번째 사용자 요청 실행
            Future<ResultData> second = requests.submit(() -> service.searchBooks(72L, "동시 검색 테스트", 1));
            // 첫 응답 완료 전에 공유 대상 외부 요청이 시작된 사실 확인
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            // 두 사용자 요청이 모두 공유 작업 조회에 도달한 사실 확인
            assertTrue(sharing.await(5, TimeUnit.SECONDS));
            // 동일 검색의 공급자 HTTP 호출이 한 건인지 확인
            assertEquals(1, calls.get());
            // 공급자 응답 완료 허용
            release.countDown();
            // 첫 검색 요청의 정상 응답 확인
            assertEquals(200, first.get(5, TimeUnit.SECONDS).getCode());
            // 두 번째 검색 요청의 정상 응답 확인
            assertEquals(200, second.get(5, TimeUnit.SECONDS).getCode());
            // 저장 이후의 순차 요청은 캐시를 정상 재사용하는지 확인
            assertEquals(200, service.searchBooks(73L, "동시 검색 테스트", 1).getCode());
            // 캐시 적중 이후에는 추가 공급자 요청이 없는지 확인
            assertEquals(1, calls.get());
            // 공유한 두 요청 중 실제 공급자 호출 한 건만 쿼터를 예약한 사실 확인
            verify(bookSearchProtectionService).reserveProviderCall(anyLong(), eq("kakao"));
        }

        // 서버와 요청 실행기를 테스트 종료 시 정리
        finally {
            // 공급자 응답 대기 해제
            release.countDown();
            // 임시 HTTP 서버 중지
            server.stop(0);
            // 사용자 요청 실행기 종료
            requests.shutdownNow();
            // 공급자 실행기 종료
            provider.shutdownNow();
            // 요청 스레드 정리 확인
            assertTrue(requests.awaitTermination(5, TimeUnit.SECONDS));
            // 공급자 스레드 정리 확인
            assertTrue(provider.awaitTermination(5, TimeUnit.SECONDS));
        }

    }

    // 외부 도서 검색 API 통신 객체
    @Mock
    private RestTemplate restTemplate;
    // 회원별 제한과 공용 캐시 및 공급자별 쿼터 보호 서비스 대역
    @Mock
    private BookSearchProtectionService bookSearchProtectionService;
    // 로그인 회원의 저장 언어 설정 조회 Mapper 대역
    @Mock
    private UserMapper userMapper;
    // 외부 도서 검색 요청 URI 검증 객체
    @Captor
    private ArgumentCaptor<URI> uriCaptor;
    // 외부 도서 검색 인증 헤더 검증 객체
    @Captor
    private ArgumentCaptor<HttpEntity<?>> httpEntityCaptor;

    // 언어별 외부 도서 검색 서비스
    private BookSearchService bookSearchService;
    // 화면 응답 필드명 검증 객체
    private ObjectMapper objectMapper;

    /** 각 테스트에서 언어별 도서 검색 서비스와 설정값을 구성 */
    @BeforeEach
    void setUp() {
        // 외부 JSON 응답을 변환할 객체를 생성
        objectMapper = new ObjectMapper();
        // 도서 검색 테스트 대상을 생성
        bookSearchService = new BookSearchService(
                restTemplate, objectMapper, bookSearchProtectionService, userMapper
        );
        // 테스트 요청이 사용할 카카오 도서 검색 주소를 설정
        ReflectionTestUtils.setField(bookSearchService, "bookSearchUrl", "https://dapi.kakao.com/v3/search/book");
        // 테스트 요청이 사용할 가상 카카오 REST API 키를 설정
        ReflectionTestUtils.setField(bookSearchService, "kakaoRestApiKey", "test-rest-key");
        // 테스트 요청이 사용할 Google Books 주소를 설정
        ReflectionTestUtils.setField(bookSearchService, "googleBooksUrl", "https://www.googleapis.com/books/v1/volumes");
        // 테스트 요청이 사용할 가상 Google Books API 키를 설정
        ReflectionTestUtils.setField(bookSearchService, "googleBooksApiKey", "test-google-key");
        // 공통 실패 응답이 사용할 테스트 메시지 소스를 생성
        StaticMessageSource messageSource = new StaticMessageSource();
        // 검색 실패 코드의 테스트용 사용자 문구를 등록
        messageSource.addMessage("common.alert.0008", Locale.KOREAN, "검색에 실패했어요.");
        // 검색 요청 제한 코드의 테스트용 사용자 문구를 등록
        messageSource.addMessage("book.alert.0001", Locale.KOREAN, "검색 요청이 너무 많아요.");
        // 비속어 검색 차단의 테스트용 사용자 문구를 등록
        messageSource.addMessage("common.alert.0015", Locale.KOREAN, "비속어가 포함되어 있어요.");
        // 공통 메시지 조회 로케일을 등록한 한국어 문구와 일치시킴
        LocaleContextHolder.setLocale(Locale.KOREAN);
        // 공통 실패 응답에서 테스트 메시지를 조회할 수 있도록 설정
        new MessageUtils().setMessageSource(messageSource);
        // 공유 작업도 운영 공용 캐시와 같은 정규화 및 해시 키 사용
        lenient().when(bookSearchProtectionService.getCacheKey(any(), any(), anyInt())).thenCallRealMethod();
    }

    /** 공백으로 우회한 비속어를 외부 검색과 Redis 처리 전에 차단하는지 검증 */
    @Test
    void blocksSpacedBadWord() {
        // 공통 비속어 검사에서 공백 우회 검색어를 탐지하도록 구성
        when(bookSearchProtectionService.findBlockedSearchKeyword("시 발")).thenReturn(Optional.of("시발"));

        // 공백 우회 비속어로 도서 검색을 요청
        ResultData resultData = bookSearchService.searchBooks(7L, "시 발", 1);

        // 화면에 비속어 오류를 반환하고 계정 설정과 외부 검색 처리에는 진입하지 않는지 확인
        assertEquals(2015, resultData.getCode());
        verifyNoInteractions(userMapper, restTemplate);
        verify(bookSearchProtectionService, never()).getCachedSearch(any(), any(), any(Integer.class));
    }

    /** 한국어 설정에서 카카오 검색 결과와 50권 페이지 계약을 유지하는지 검증 */
    @Test
    void searchesKakaoInKorean() {
        String responseBody = """
                {
                  "meta": {"is_end": false, "pageable_count": 20, "total_count": 20},
                  "documents": [{
                    "authors": ["기시미 이치로", "고가 후미타케"],
                    "contents": "도서 소개",
                    "datetime": "2014-11-17T00:00:00.000+09:00",
                    "isbn": "8996991341 9788996991342",
                    "publisher": "인플루엔셜",
                    "thumbnail": "https://search1.kakaocdn.net/thumb/R120x174.q85/?fname=http%3A%2F%2Ft1.daumcdn.net%2Flbook%2Fimage%2F6253040",
                    "title": "미움받을 용기"
                  }]
                }
                """;
        // 한국어 사용 계정 설정을 구성
        when(userMapper.getUserSettingDtl(7L)).thenReturn(getSetting("N"));
        // 카카오 외부 요청과 검색 보호 허용 결과를 구성
        when(bookSearchProtectionService.isRequestAllowed(7L, false)).thenReturn(true);
        when(bookSearchProtectionService.reserveProviderCall(7L, "kakao")).thenReturn(true);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        // 카카오 두 번째 페이지에 대응하는 시작 위치로 검색
        ResultData resultData = bookSearchService.searchBooks(7L, "미움받을 용기", 51);
        BookSearchResponseDto searchResult = assertInstanceOf(BookSearchResponseDto.class, resultData.getData());
        BookJsonDto.BookDto bookDto = assertInstanceOf(BookJsonDto.BookDto.class, searchResult.getBookList().get(0));

        // 기존 카카오 화면 계약과 한국어 도서 코드가 유지되는지 확인
        assertEquals(200, resultData.getCode());
        assertEquals(101, searchResult.getNextStart());
        assertEquals("ko", bookDto.getLangCode());
        assertEquals("9788996991342", bookDto.getIsbn());
        assertEquals("20141117", bookDto.getPubdate());
        // 카카오 인증과 50권 두 번째 페이지 요청을 확인
        verify(restTemplate).exchange(uriCaptor.capture(), eq(HttpMethod.GET), httpEntityCaptor.capture(), eq(String.class));
        assertEquals("KakaoAK test-rest-key", httpEntityCaptor.getValue().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        assertTrue(uriCaptor.getValue().getQuery().contains("page=2"));
        assertTrue(uriCaptor.getValue().getQuery().contains("size=50"));
        // 공급자별 화면 응답 캐시 저장을 확인
        verify(bookSearchProtectionService).setCachedSearch(
                eq("kakao"), eq("미움받을 용기"), eq(51), any(BookSearchResponseDto.class)
        );
    }

    /** 영어 설정에서 Google Books 검색과 40권 페이지 및 ISBN13 변환을 검증 */
    @Test
    void searchesGoogleInEnglish() {
        String responseBody = """
                {
                  "totalItems": 100,
                  "items": [{
                    "id": "google-volume-id",
                    "volumeInfo": {
                      "title": "Clean Architecture",
                      "authors": ["Robert C. Martin"],
                      "publisher": "Pearson",
                      "publishedDate": "2018-04",
                      "description": "A guide to software architecture",
                      "industryIdentifiers": [
                        {"type": "ISBN_10", "identifier": "0134494164"},
                        {"type": "ISBN_13", "identifier": "9780134494166"}
                      ],
                      "imageLinks": {
                        "smallThumbnail": "http://books.google.com/small.jpg",
                        "thumbnail": "http://books.google.com/cover.jpg"
                      },
                      "language": "en"
                    }
                  }]
                }
                """;
        // 영어 사용 계정 설정을 구성
        when(userMapper.getUserSettingDtl(7L)).thenReturn(getSetting("Y"));
        // Google Books 외부 요청과 검색 보호 허용 결과를 구성
        when(bookSearchProtectionService.isRequestAllowed(7L, false)).thenReturn(true);
        when(bookSearchProtectionService.reserveProviderCall(7L, "google")).thenReturn(true);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        // Google Books 두 번째 페이지에 대응하는 시작 위치로 검색
        ResultData resultData = bookSearchService.searchBooks(7L, "Clean Architecture", 41);
        BookSearchResponseDto searchResult = assertInstanceOf(BookSearchResponseDto.class, resultData.getData());
        BookJsonDto.BookDto bookDto = searchResult.getBookList().get(0);

        // Google Books 화면 계약과 영어 도서 코드를 확인
        assertEquals(200, resultData.getCode());
        assertEquals(81, searchResult.getNextStart());
        assertEquals("en", bookDto.getLangCode());
        assertEquals("9780134494166", bookDto.getIsbn());
        assertEquals("201804", bookDto.getPubdate());
        assertEquals("https://books.google.com/cover.jpg", bookDto.getImage());
        // Google Books의 0부터 시작하는 인덱스와 영어 제한 및 최대 40건 요청을 확인
        verify(restTemplate).exchange(uriCaptor.capture(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
        String requestQuery = uriCaptor.getValue().getQuery();
        assertTrue(requestQuery.contains("startIndex=40"));
        assertTrue(requestQuery.contains("maxResults=40"));
        assertTrue(requestQuery.contains("langRestrict=en"));
        assertTrue(requestQuery.contains("printType=books"));
        assertTrue(requestQuery.contains("key=test-google-key"));
        // Google 공급자 전용 캐시 저장을 확인
        verify(bookSearchProtectionService).setCachedSearch(
                eq("google"), eq("Clean Architecture"), eq(41), any(BookSearchResponseDto.class)
        );
    }

    /** 공급자별 공용 캐시에 적중하면 실제 외부 호출 예산을 사용하지 않는지 검증 */
    @Test
    void skipsCallOnCacheHit() {
        // 영어 사용 계정과 Google 공급자 캐시 결과를 구성
        when(userMapper.getUserSettingDtl(7L)).thenReturn(getSetting("Y"));
        BookSearchResponseDto cachedResult = new BookSearchResponseDto(List.of(), true, null);
        when(bookSearchProtectionService.getCachedSearch("google", "book", 1)).thenReturn(cachedResult);
        when(bookSearchProtectionService.isRequestAllowed(7L, true)).thenReturn(true);

        // 공용 캐시가 있는 영어 도서 검색을 실행
        ResultData resultData = bookSearchService.searchBooks(7L, "book", 1);

        // 외부 호출 없이 캐시 결과가 성공 응답으로 반환되는지 확인
        assertEquals(200, resultData.getCode());
        verifyNoInteractions(restTemplate);
        verify(bookSearchProtectionService, never()).reserveProviderCall(7L, "google");
    }

    /** Google Books 인증 오류가 사용자 공통 검색 실패 코드로 변환되는지 검증 */
    @Test
    void handlesGoogleBooksFailure() {
        // 영어 사용 계정과 Google Books 인증 오류 흐름을 구성
        when(userMapper.getUserSettingDtl(7L)).thenReturn(getSetting("Y"));
        when(bookSearchProtectionService.isRequestAllowed(7L, false)).thenReturn(true);
        when(bookSearchProtectionService.reserveProviderCall(7L, "google")).thenReturn(true);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // 인증 오류가 발생하는 영어 도서 검색을 실행
        ResultData resultData = bookSearchService.searchBooks(7L, "book", 1);

        // 외부 오류 원문 대신 기존 사용자 공통 검색 실패 코드를 확인
        assertEquals(2008, resultData.getCode());
    }

    /** 테스트에서 사용할 사용자 언어 설정을 생성 */
    private UserSettingDto getSetting(String englishYsno) {
        // 영어 사용 여부를 포함한 설정 DTO를 생성
        UserSettingDto setting = new UserSettingDto();
        setting.setEnglishYsno(englishYsno);
        // 계정 언어 분기에서 사용할 설정을 반환
        return setting;
    }
}
