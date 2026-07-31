package org.our.sadari.book.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
 * author         : SeungHyeon.Kang
 * date           : 2026-07-31
 * description    : 카카오 도서 검색 요청과 기존 사용자 화면 응답 변환을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-31        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class BookSearchServiceTest {

    // 외부 도서 검색 API 통신 객체
    @Mock
    private RestTemplate restTemplate;

    // 카카오 도서 검색 요청 URI 검증 객체
    @Captor
    private ArgumentCaptor<URI> uriCaptor;

    // 카카오 도서 검색 인증 헤더 검증 객체
    @Captor
    private ArgumentCaptor<HttpEntity<?>> httpEntityCaptor;

    // 카카오 도서 검색 서비스
    private BookSearchService bookSearchService;

    /**
     * 각 테스트에서 카카오 도서 검색 서비스와 설정값을 구성한다
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // 카카오 JSON 응답을 변환할 객체를 생성한다
        ObjectMapper objectMapper = new ObjectMapper();
        // 도서 검색 테스트 대상을 생성한다
        bookSearchService = new BookSearchService(restTemplate, objectMapper);
        // 테스트 요청이 사용할 카카오 도서 검색 주소를 설정한다
        ReflectionTestUtils.setField(bookSearchService, "bookSearchUrl", "https://dapi.kakao.com/v3/search/book");
        // 테스트 요청이 사용할 가상 카카오 REST API 키를 설정한다
        ReflectionTestUtils.setField(bookSearchService, "kakaoRestApiKey", "test-rest-key");
        // 공통 실패 응답이 사용할 테스트 메시지 소스를 생성한다
        StaticMessageSource messageSource = new StaticMessageSource();
        // 검색 실패 코드의 테스트용 사용자 문구를 등록한다
        messageSource.addMessage("common.alert.0008", Locale.KOREAN, "검색에 실패했어요.");
        // 공통 메시지 조회 로케일을 등록한 한국어 문구와 일치시킨다
        LocaleContextHolder.setLocale(Locale.KOREAN);
        // 공통 실패 응답에서 테스트 메시지를 조회할 수 있도록 설정한다
        new MessageUtils().setMessageSource(messageSource);
    }

    /**
     * 카카오 도서 응답을 기존 화면 필드와 페이지 계약으로 변환하는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void searchBooksMapsKakaoResponseToExistingScreenContract() {
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
                      "thumbnail": "https://search1.kakaocdn.net/thumb/R120x174.q85/book-cover",
                      "title": "미움받을 용기"
                    }
                  ]
                }
                """;

        // 카카오 도서 검색 성공 응답을 설정한다
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        // 기존 화면의 두 번째 검색 시작 위치로 도서 목록을 조회한다
        ResultData resultData = bookSearchService.searchBooks("미움받을 용기", 11);
        // 검색 결과 목록과 첫 번째 도서 정보를 구체적인 타입으로 확인한다
        List<?> bookList = assertInstanceOf(List.class, resultData.getData());
        // 첫 번째 검색 결과를 사용자 화면 도서 타입으로 확인한다
        BookJsonDto.BookDto bookDto = assertInstanceOf(BookJsonDto.BookDto.class, bookList.get(0));

        // 도서 검색 성공 코드를 확인한다
        assertEquals(200, resultData.getCode());
        // 저자 배열이 기존 구분 문자열로 변환되었는지 확인한다
        assertEquals("기시미 이치로^고가 후미타케", bookDto.getAuthor());
        // ISBN10과 ISBN13 중 기존 데이터와 호환되는 ISBN13이 선택되었는지 확인한다
        assertEquals("9788996991342", bookDto.getIsbn());
        // 카카오 도서 표지 URL이 기존 화면 필드에 설정되었는지 확인한다
        assertEquals("https://search1.kakaocdn.net/thumb/R120x174.q85/book-cover", bookDto.getImage());
        // 카카오 도서 소개가 기존 화면 필드에 설정되었는지 확인한다
        assertEquals("도서 소개", bookDto.getDescription());
        // ISO 8601 출간일이 기존 yyyyMMdd 형식으로 변환되었는지 확인한다
        assertEquals("20141117", bookDto.getPubdate());

        // 기존 시작 위치와 인증 키가 카카오 요청 계약으로 변환되었는지 확인한다
        verify(restTemplate).exchange(uriCaptor.capture(), eq(HttpMethod.GET), httpEntityCaptor.capture(), eq(String.class));
        // 카카오 도서 검색 인증 헤더가 요청에 포함되었는지 확인한다
        assertEquals("KakaoAK test-rest-key", httpEntityCaptor.getValue().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        // 두 번째 페이지가 요청 URI에 포함되었는지 확인한다
        assertTrue(uriCaptor.getValue().getQuery().contains("page=2"));
        // 화면 표시 건수가 요청 URI에 포함되었는지 확인한다
        assertTrue(uriCaptor.getValue().getQuery().contains("size=10"));
    }

    /**
     * 카카오 인증 오류가 사용자 공통 검색 실패 코드로 변환되는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void searchBooksReturnsCommonFailureWhenKakaoRejectsRequest() {
        // 카카오 API가 인증 오류를 반환하는 외부 통신 흐름을 설정한다
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // 인증 오류가 발생하는 도서 검색을 실행한다
        ResultData resultData = bookSearchService.searchBooks("도서", 1);

        // 외부 오류 원문 대신 기존 사용자 공통 검색 실패 코드를 확인한다
        assertEquals(2008, resultData.getCode());
    }
}
