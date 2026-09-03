package org.our.sadari.book.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.book.dto.BookJsonDto;
import org.our.sadari.book.dto.BookSearchResponseDto;
import org.our.sadari.book.dto.KakaoBookJsonDto;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.util.MessageUtils;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * fileName       : BookSearchServiceTest
 * author         : HanWon.Jang
 * date           : 2026-07-31
 * description    : 카카오 도서 검색 요청과 기존 사용자 화면 응답 변환을 검증함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-31        SeungHyeon.Kang    최초 생성
 * 2026-08-16        SeungHyeon.Kang    도서 검색·인기 검색어 검증 추가
 * 2026-08-28        HanWon.Jang        캐시 유형별 단기 한도 검증
 */
@ExtendWith(MockitoExtension.class)
class BookSearchServiceTest {

    // 외부 도서 검색 API 통신 객체
    @Mock
    private RestTemplate restTemplate;

    // 회원별 제한과 공용 캐시 및 앱 전체 쿼터 보호 서비스 대역
    @Mock
    private BookSearchProtectionService bookSearchProtectionService;

    // 카카오 도서 검색 요청 URI 검증 객체
    @Captor
    private ArgumentCaptor<URI> uriCaptor;

    // 카카오 도서 검색 인증 헤더 검증 객체
    @Captor
    private ArgumentCaptor<HttpEntity<?>> httpEntityCaptor;

    // 카카오 도서 검색 서비스
    private BookSearchService bookSearchService;
    // 화면 응답 필드명 검증 객체
    private ObjectMapper objectMapper;

    /**
     * 각 테스트에서 카카오 도서 검색 서비스와 설정값을 구성함
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // 카카오 JSON 응답을 변환할 객체를 생성함
        objectMapper = new ObjectMapper();
        // 도서 검색 테스트 대상을 생성함
        bookSearchService = new BookSearchService(restTemplate, objectMapper, bookSearchProtectionService);
        // 테스트 요청이 사용할 카카오 도서 검색 주소를 설정함
        ReflectionTestUtils.setField(bookSearchService, "bookSearchUrl", "https://dapi.kakao.com/v3/search/book");
        // 테스트 요청이 사용할 가상 카카오 REST API 키를 설정함
        ReflectionTestUtils.setField(bookSearchService, "kakaoRestApiKey", "test-rest-key");
        // 공통 실패 응답이 사용할 테스트 메시지 소스를 생성함
        StaticMessageSource messageSource = new StaticMessageSource();
        // 검색 실패 코드의 테스트용 사용자 문구를 등록함
        messageSource.addMessage("common.alert.0008", Locale.KOREAN, "검색에 실패했어요.");
        // 검색 요청 제한 코드의 테스트용 사용자 문구를 등록함
        messageSource.addMessage("book.alert.0001", Locale.KOREAN, "검색 요청이 너무 많아요.");
        // 공통 메시지 조회 로케일을 등록한 한국어 문구와 일치시킴
        LocaleContextHolder.setLocale(Locale.KOREAN);
        // 공통 실패 응답에서 테스트 메시지를 조회할 수 있도록 설정함
        new MessageUtils().setMessageSource(messageSource);
    }

    /**
     * 카카오 도서 응답을 기존 화면 필드와 페이지 계약으로 변환하는지 검증함
     *
     * @author HanWon.Jang
     * @throws Exception 화면 응답 JSON 직렬화에 실패한 경우 발생
     */
    @Test
    void getBooksMapsKakaoResult() throws Exception {
        String responseBody = """
                {
                  "meta": {"is_end": false, "pageable_count": 20, "total_count": 20},
                  "documents": [
                    {
                      "authors": ["기시미 이치로", "고가 후미타케"],
                      "contents": "도서 소개",
                      "datetime": "2014-11-17T00:00:00.000+09:00",
                      "isbn": "8996991341 9788996991342",
                      "publisher": "인플루엔셜",
                      "thumbnail": "https://search1.kakaocdn.net/thumb/R120x174.q85/?fname=http%3A%2F%2Ft1.daumcdn.net%2Flbook%2Fimage%2F6253040%3Ftimestamp%3D20260115151223",
                      "title": "미움받을 용기"
                    }
                  ]
                }
                """;

        // 카카오 도서 검색 성공 응답을 설정함
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(responseBody));
        // 로그인 회원의 캐시 미적중 단기 검색 요청을 허용함
        when(bookSearchProtectionService.isRequestAllowed(7L, false)).thenReturn(true);
        // 공용 캐시가 없는 카카오 실제 호출을 회원별 및 앱 전체 보호 한도 안에서 허용함
        when(bookSearchProtectionService.reserveProviderCall(7L)).thenReturn(true);

        // 50권 단위의 두 번째 검색 시작 위치로 도서 목록을 조회함
        ResultData resultData = bookSearchService.searchBooks(7L, "미움받을 용기", 51);
        // 검색 결과 목록과 다음 페이지 정보를 구체적인 타입으로 확인함
        BookSearchResponseDto searchResult = assertInstanceOf(BookSearchResponseDto.class, resultData.getData());
        // 50권 페이지 응답에서 화면에 전달할 도서 목록을 조회함
        List<?> bookList = searchResult.getBookList();
        // 첫 번째 검색 결과를 사용자 화면 도서 타입으로 확인함
        BookJsonDto.BookDto bookDto = assertInstanceOf(BookJsonDto.BookDto.class, bookList.get(0));

        // 도서 검색 성공 코드를 확인함
        assertEquals(200, resultData.getCode());
        // 카카오 메타정보의 다음 페이지 존재 여부가 화면 응답에 유지되는지 확인함
        assertEquals(false, searchResult.isEnd());
        // 다음 카카오 50권 페이지의 기존 시작 위치가 계산되었는지 확인함
        assertEquals(101, searchResult.getNextStart());
        // 저자 배열이 기존 구분 문자열로 변환되었는지 확인함
        assertEquals("기시미 이치로^고가 후미타케", bookDto.getAuthor());
        // ISBN10과 ISBN13 중 기존 데이터와 호환되는 ISBN13이 선택되었는지 확인함
        assertEquals("9788996991342", bookDto.getIsbn());
        // 카카오 썸네일의 Daum 원본 표지 URL이 기존 image 필드에 설정되었는지 확인함
        assertEquals("https://t1.daumcdn.net/lbook/image/6253040?timestamp=20260115151223", bookDto.getImage());
        // 원본 표지 실패 시 사용할 공식 카카오 썸네일이 별도 필드에 유지되는지 확인함
        assertEquals(
                "https://search1.kakaocdn.net/thumb/R120x174.q85/?fname=http%3A%2F%2Ft1.daumcdn.net%2Flbook%2Fimage%2F6253040%3Ftimestamp%3D20260115151223",
                bookDto.getThumbnailImage()
        );
        // 외부 thumbnail 필드가 기존 image 응답 필드명을 바꾸지 않는지 직렬화 결과를 확인함
        String serializedBook = objectMapper.writeValueAsString(bookDto);
        // 기존 프론트엔드 계약의 image 필드가 응답에 유지되는지 확인함
        assertTrue(serializedBook.contains("\"image\":\"https://t1.daumcdn.net/lbook/image/6253040"));
        // 카카오 도서 소개가 기존 화면 필드에 설정되었는지 확인함
        assertEquals("도서 소개", bookDto.getDescription());
        // ISO 8601 출간일이 기존 yyyyMMdd 형식으로 변환되었는지 확인함
        assertEquals("20141117", bookDto.getPubdate());

        // 기존 시작 위치와 인증 키가 카카오 요청 계약으로 변환되었는지 확인함
        verify(restTemplate).exchange(uriCaptor.capture(), eq(HttpMethod.GET), httpEntityCaptor.capture(), eq(String.class));
        // 카카오 도서 검색 인증 헤더가 요청에 포함되었는지 확인함
        assertEquals("KakaoAK test-rest-key", httpEntityCaptor.getValue().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        // 두 번째 페이지가 요청 URI에 포함되었는지 확인함
        assertTrue(uriCaptor.getValue().getQuery().contains("page=2"));
        // 카카오 요청당 최대 조회 건수가 요청 URI에 포함되었는지 확인함
        assertTrue(uriCaptor.getValue().getQuery().contains("size=50"));
        // 카카오 원문 결과가 같은 검색어의 반복 호출을 줄일 공용 캐시에 저장되는지 확인함
        verify(bookSearchProtectionService).setCachedSearch(eq("미움받을 용기"), eq(2), any(KakaoBookJsonDto.class));
        // 추가 페이지 조회가 같은 검색어의 인기 점수를 반복 증가시키지 않는지 확인함
        verify(bookSearchProtectionService, never()).setPopularKeyword(7L, "미움받을 용기");
    }

    /**
     * 결과가 존재하는 첫 페이지 검색만 인기 검색어 후보에 반영하는지 검증함
     *
     * @author HanWon.Jang
     * @throws Exception 테스트용 카카오 검색 응답 변환에 실패한 경우 발생
     */
    @Test
    void ranksFirstPageKeyword() throws Exception {
        String cachedJson = """
                {
                  "meta": {"is_end": true, "pageable_count": 1, "total_count": 1},
                  "documents": [
                    {
                      "authors": ["헤르만 헤세"],
                      "contents": "한 소년의 성장 이야기",
                      "datetime": "2009-01-20T00:00:00.000+09:00",
                      "isbn": "9788937460449",
                      "publisher": "민음사",
                      "thumbnail": "https://search1.kakaocdn.net/thumb/R120x174.q85/book-cover",
                      "title": "데미안"
                    }
                  ]
                }
                """;
        // 외부 호출 없이 사용할 검색 결과가 있는 첫 페이지 공용 캐시를 생성함
        KakaoBookJsonDto cachedResult = objectMapper.readValue(cachedJson, KakaoBookJsonDto.class);
        // 로그인 회원의 캐시 적중 단기 검색 요청을 허용함
        when(bookSearchProtectionService.isRequestAllowed(7L, true)).thenReturn(true);
        // 같은 검색어의 첫 페이지가 공용 캐시에 존재하도록 설정함
        when(bookSearchProtectionService.getCachedSearch("데미안", 1)).thenReturn(cachedResult);

        // 결과가 있는 첫 페이지 도서 검색을 실행함
        ResultData resultData = bookSearchService.searchBooks(7L, "데미안", 1);

        // 캐시 적중 검색도 정상 성공 응답을 유지하는지 확인함
        assertEquals(200, resultData.getCode());
        // 결과가 존재하는 첫 페이지의 검색 의도가 인기 검색어 집계에 전달되는지 확인함
        verify(bookSearchProtectionService).setPopularKeyword(7L, "데미안");
        // 인기 검색어 집계를 위해 카카오 외부 호출을 추가하지 않는지 확인함
        verifyNoInteractions(restTemplate);
    }

    /**
     * 카카오 인증 오류가 사용자 공통 검색 실패 코드로 변환되는지 검증함
     *
     * @author HanWon.Jang
     */
    @Test
    void getBooksKakaoFailure() {
        // 로그인 회원의 캐시 미적중 단기 검색 요청을 허용함
        when(bookSearchProtectionService.isRequestAllowed(7L, false)).thenReturn(true);
        // 카카오 오류 응답도 공급자 쿼터를 사용할 수 있어 회원별 및 앱 전체 실제 호출을 먼저 예약함
        when(bookSearchProtectionService.reserveProviderCall(7L)).thenReturn(true);
        // 카카오 API가 인증 오류를 반환하는 외부 통신 흐름을 설정함
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // 인증 오류가 발생하는 도서 검색을 실행함
        ResultData resultData = bookSearchService.searchBooks(7L, "도서", 1);

        // 외부 오류 원문 대신 기존 사용자 공통 검색 실패 코드를 확인함
        assertEquals(2008, resultData.getCode());
    }

    /**
     * 회원별 검색 제한을 초과하면 카카오 API를 호출하지 않는지 검증함
     *
     * @author HanWon.Jang
     */
    @Test
    void blocksKakaoOnRateLimit() {
        // 로그인 회원이 캐시 미적중 단기 검색 제한에 도달하도록 설정함
        when(bookSearchProtectionService.isRequestAllowed(7L, false)).thenReturn(false);

        // 제한에 도달한 회원의 도서 검색을 실행함
        ResultData resultData = bookSearchService.searchBooks(7L, "도서", 1);

        // 사용자에게 도서 검색 요청 제한 코드를 반환하는지 확인함
        assertEquals(2024, resultData.getCode());
        // 제한된 요청이 카카오 외부 통신을 사용하지 않았는지 확인함
        verifyNoInteractions(restTemplate);
    }

    /**
     * 공용 검색 캐시에 적중하면 앱 전체 실제 호출 예산과 카카오 API를 사용하지 않는지 검증함
     *
     * @author HanWon.Jang
     */
    @Test
    void skipsKakaoOnCacheHit() {
        // 로그인 회원의 캐시 적중 단기 검색 요청을 허용함
        when(bookSearchProtectionService.isRequestAllowed(7L, true)).thenReturn(true);
        // 마지막 페이지인 빈 카카오 검색 결과를 생성함
        KakaoBookJsonDto cachedResult = new KakaoBookJsonDto();
        // 공용 캐시 결과에 빈 도서 목록을 설정함
        cachedResult.setDocuments(List.of());
        // 마지막 페이지 메타정보를 생성함
        KakaoBookJsonDto.MetaDto meta = new KakaoBookJsonDto.MetaDto();
        // 캐시 적중 뒤 불필요한 다음 페이지를 호출하지 않도록 마지막 페이지를 설정함
        meta.setEnd(true);
        // 공용 캐시 결과에 카카오 페이지 메타정보를 설정함
        cachedResult.setMeta(meta);
        // 같은 검색어의 첫 페이지가 공용 캐시에 존재하도록 설정함
        when(bookSearchProtectionService.getCachedSearch("도서", 1)).thenReturn(cachedResult);

        // 공용 캐시가 있는 도서 검색을 실행함
        ResultData resultData = bookSearchService.searchBooks(7L, "도서", 1);

        // 외부 호출 없이 캐시 결과가 성공 응답으로 반환되는지 확인함
        assertEquals(200, resultData.getCode());
        // 캐시 적중 요청이 카카오 외부 통신을 사용하지 않았는지 확인함
        verifyNoInteractions(restTemplate);
        // 캐시 적중 요청이 회원별 일간 및 앱 전체 실제 호출 한도를 차감하지 않는지 확인함
        verify(bookSearchProtectionService, never()).reserveProviderCall(7L);
    }
}
