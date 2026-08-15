package org.our.sadari.book.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.book.dto.BookJsonDto;
import org.our.sadari.book.dto.BookSearchResponseDto;
import org.our.sadari.book.dto.KakaoBookJsonDto;
import org.our.sadari.book.util.BookCoverUrlUtil;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * fileName       : BookSearchService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-06
 * description    : 외부 도서 검색 API를 호출하고 사용자 화면 응답으로 변환한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-06        SeungHyeon.Kang    최초 생성
 * 2026-07-31        SeungHyeon.Kang    종료된 네이버 API를 카카오 도서 검색 API로 교체
 * 2026-08-16        SeungHyeon.Kang    50권 조회와 Redis 쿼터 보호 및 공용 캐시 적용
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookSearchService {

    // 카카오 도서 검색 API의 요청당 최대 조회 건수
    private static final int DISPLAY_COUNT = 50;
    // 최소 시작 설정값
    private static final int MIN_START = 1;
    // 카카오 도서 검색의 최대 50페이지를 기존 시작 위치 계약으로 환산한 설정값
    private static final int MAX_START = 2451;
    // 카카오 REST API 인증 스킴
    private static final String KAKAO_AUTH_SCHEME = "KakaoAK ";

    // 카카오 도서 검색 URL 설정값
    @Value("${book.search.url}")
    private String bookSearchUrl;

    // 카카오 도서 검색 API 인증에 사용하는 REST API 키
    @Value("${kakao.key.restApi}")
    private String kakaoRestApiKey;

    // 외부 HTTP API 통신 객체
    private final RestTemplate restTemplate;
    // Object 데이터 접근 객체
    private final ObjectMapper objectMapper;
    // 회원별 검색 제한과 공용 검색 캐시 및 앱 전체 쿼터 보호 서비스
    private final BookSearchProtectionService bookSearchProtectionService;

    /**
     * 검색어 기준 카카오 도서 목록을 검색한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 도서 검색을 요청한 로그인 회원 번호
     * @param query 카카오 도서 API에 전달할 검색어
     * @param start 기존 화면 계약에서 사용하는 검색 결과 시작 위치
     * @return 사용자 화면 형식으로 변환된 도서 검색 결과
     */
    public ResultData searchBooks(Long userNumb, String query, int start) {
        // 인증값과 검색어 및 50권 페이지 경계가 올바르지 않으면 외부 요청 전에 차단한다
        if (StringUtil.hasEmpty(userNumb, query) || start < MIN_START || start > MAX_START
                || (start - MIN_START) % DISPLAY_COUNT != 0) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 악성 반복 요청과 Redis 장애가 카카오 일일 쿼터를 소모하지 않도록 회원 제한을 먼저 검사한다
        if (!bookSearchProtectionService.isRequestAllowed(userNumb)) {
            // "검색 요청이 너무 많아요. 잠시 후 다시 시도해주세요."
            return ResultData.fail(ResultEnum.BOOK_SEARCH_RATE_LIMITED);
        }

        // 카카오 API 통신과 응답 변환 실패를 공통 검색 실패 응답으로 격리한다
        try {
            // 기존 시작 위치를 카카오 API의 1부터 시작하는 50권 페이지 번호로 변환한다
            int page = ((start - MIN_START) / DISPLAY_COUNT) + 1;
            // 동일 검색어와 페이지의 짧은 공용 캐시를 먼저 조회한다
            KakaoBookJsonDto kakaoBookJsonDto = bookSearchProtectionService.getCachedSearch(query, page);

            // 공용 캐시에 검색 결과가 없을 때만 카카오 일일 쿼터를 예약하고 외부 API를 호출한다
            if (StringUtil.isEmpty(kakaoBookJsonDto)) {
                // 앱 전체 실제 호출이 비상 여유를 침범하면 카카오 요청 전에 차단한다
                if (!bookSearchProtectionService.reserveProviderCall()) {
                    // "검색 요청이 너무 많아요. 잠시 후 다시 시도해주세요."
                    return ResultData.fail(ResultEnum.BOOK_SEARCH_RATE_LIMITED);
                }

                // 사용자 검색어로 카카오 도서 검색 API에서 최대 50권을 호출한다
                ResponseEntity<String> response = requestKakaoBookSearch(query.trim(), page);

                // 본문이 없는 외부 응답은 정상 검색 결과로 해석하지 않는다
                if (StringUtil.isEmpty(response.getBody())) {
                    // "검색에 실패했어요.\n다시 시도해주세요."
                    return ResultData.fail(ResultEnum.COMMON_SEARCH_REJECTED);
                }

                // 카카오 원문 응답을 외부 API 전용 DTO로 역직렬화한다
                kakaoBookJsonDto = objectMapper.readValue(response.getBody(), KakaoBookJsonDto.class);
                // 같은 검색어의 반복 호출이 카카오 쿼터를 다시 소모하지 않도록 공용 캐시에 저장한다
                bookSearchProtectionService.setCachedSearch(query, page, kakaoBookJsonDto);
            }

            // 외부 필드명이 화면 응답 필드명을 바꾸지 않도록 명시적인 화면 DTO로 변환한다
            List<BookJsonDto.BookDto> bookList = getBookList(kakaoBookJsonDto.getDocuments());
            // 카카오 메타정보가 없으면 추가 호출로 쿼터를 소모하지 않도록 마지막 페이지로 처리한다
            boolean isEnd = StringUtil.isEmpty(kakaoBookJsonDto.getMeta()) || kakaoBookJsonDto.getMeta().isEnd()
                    || start == MAX_START;
            // 마지막 페이지가 아닐 때만 다음 50권 검색의 기존 시작 위치를 계산한다
            Integer nextStart = isEnd ? null : start + DISPLAY_COUNT;
            // 화면이 10권씩 나눠 표시할 최대 50권과 정확한 다음 페이지 정보를 반환한다
            return ResultData.success(new BookSearchResponseDto(bookList, isEnd, nextStart));
        }

        // 인증, 호출량 또는 요청 오류는 비밀값과 원문 응답을 제외한 상태 코드만 기록한다
        catch (RestClientResponseException e) {
            // 운영에서 외부 API 거절 원인을 구분할 수 있도록 HTTP 상태를 기록한다
            log.error("카카오 도서 검색 API가 오류 응답을 반환했습니다. 상태 코드={}", e.getStatusCode().value());
            // "검색에 실패했어요.\n다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_SEARCH_REJECTED);
        }

        // 외부 통신 또는 JSON 계약 오류는 사용자에게 내부 예외를 노출하지 않고 공통 실패로 전환한다
        catch (RestClientException | JsonProcessingException e) {
            // 운영에서 통신 장애와 응답 계약 오류를 추적할 수 있도록 예외를 기록한다
            log.error("카카오 도서 검색 API 연동에 실패했습니다.", e);
            // "검색에 실패했어요.\n다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_SEARCH_REJECTED);
        }
    }

    /**
     * 검색어와 페이지 번호로 카카오 도서 검색 API에서 최대 50권을 호출한다
     *
     * @author SeungHyeon.Kang
     * @param query 카카오 도서 API에 전달할 검색어
     * @param page 카카오 도서 검색 페이지 번호
     * @return 카카오 도서 검색 API의 HTTP 응답
     */
    private ResponseEntity<String> requestKakaoBookSearch(String query, int page) {
        // 카카오 인증 헤더를 담을 객체를 생성한다
        HttpHeaders headers = new HttpHeaders();
        // 서버 전용 REST API 키를 카카오 인증 형식으로 설정한다
        headers.set(HttpHeaders.AUTHORIZATION, KAKAO_AUTH_SCHEME + kakaoRestApiKey);

        // 검색어와 페이지 및 표시 건수를 안전하게 인코딩한 카카오 요청 URI를 생성한다
        URI uri = UriComponentsBuilder
                .fromUriString(bookSearchUrl)
                .queryParam("query", query)
                .queryParam("size", DISPLAY_COUNT)
                .queryParam("page", page)
                .queryParam("sort", "accuracy")
                .build()
                .encode()
                .toUri();

        // 인증 헤더를 포함한 카카오 도서 검색 응답을 반환한다
        return restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    /**
     * 카카오 도서 원문 목록을 사용자 화면 응답 목록으로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param kakaoBookList 카카오 도서 검색 API 원문 목록
     * @return 원본 표지와 썸네일 대체 주소를 포함한 화면 도서 목록
     */
    private List<BookJsonDto.BookDto> getBookList(List<KakaoBookJsonDto.BookDto> kakaoBookList) {
        // 카카오 응답에 documents가 없으면 빈 검색 결과로 처리한다
        if (StringUtil.isEmpty(kakaoBookList)) {
            // 화면에 전달할 빈 도서 목록을 반환한다
            return List.of();
        }

        // 카카오 원문과 화면 계약을 분리하여 저장할 결과 목록을 생성한다
        List<BookJsonDto.BookDto> bookList = new ArrayList<>();

        // 개별 카카오 도서를 화면 필드명과 값 형식으로 변환한다
        for (KakaoBookJsonDto.BookDto kakaoBook : kakaoBookList) {
            // 변환된 도서를 화면 결과 목록에 추가한다
            bookList.add(getBookDto(kakaoBook));
        }

        // 화면 계약으로 변환한 도서 목록을 반환한다
        return bookList;
    }

    /**
     * 카카오 도서 원문 항목을 기존 사용자 화면 필드로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param kakaoBook 카카오 도서 검색 API 원문 항목
     * @return 기존 image 필드와 원본 표지 주소를 유지한 화면 도서 정보
     */
    private BookJsonDto.BookDto getBookDto(KakaoBookJsonDto.BookDto kakaoBook) {
        // 화면 도서 필드를 명시적으로 설정할 응답 객체를 생성한다
        BookJsonDto.BookDto bookDto = new BookJsonDto.BookDto();
        // 카카오 도서 제목을 기존 화면 필드에 설정한다
        bookDto.setTitle(getSafeText(kakaoBook.getTitle()));
        // 카카오 저자 배열을 기존 구분 문자열로 변환하여 설정한다
        bookDto.setAuthor(getAuthor(kakaoBook.getAuthors()));
        // 카카오 출판사를 기존 화면 필드에 설정한다
        bookDto.setPublisher(getSafeText(kakaoBook.getPublisher()));
        // ISBN10과 ISBN13 중 기존 데이터와 호환되는 값을 설정한다
        bookDto.setIsbn(getIsbn(kakaoBook.getIsbn()));
        // 검증된 Daum 원본 표지를 기존 image 응답 필드에 설정한다
        bookDto.setImage(BookCoverUrlUtil.getOriginalCoverUrl(kakaoBook.getThumbnail()));
        // 원본 표지 실패 시 사용할 공식 카카오 썸네일을 별도 필드에 설정한다
        bookDto.setThumbnailImage(getSafeText(kakaoBook.getThumbnail()));
        // 카카오 도서 소개를 기존 화면 필드에 설정한다
        bookDto.setDescription(getSafeText(kakaoBook.getContents()));
        // 카카오 출간일시를 기존 yyyyMMdd 형식으로 변환하여 설정한다
        bookDto.setPubdate(getPublishedDate(kakaoBook.getDatetime()));

        // 외부 필드명과 분리된 사용자 화면 도서 정보를 반환한다
        return bookDto;
    }

    /**
     * 카카오 저자 배열을 기존 화면의 구분 문자열로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param authors 카카오 도서 저자 목록
     * @return 캐럿 문자로 구분한 저자명 또는 빈 문자열
     */
    private String getAuthor(List<String> authors) {
        // 저자가 없는 도서도 화면에서 안전하게 렌더링할 수 있도록 빈 문자열을 적용한다
        if (StringUtil.isEmpty(authors)) {
            // 비어 있는 저자명을 반환한다
            return StringUtil.EMPTY;
        }

        // 기존 화면과 저장 로직이 사용하는 저자 구분 형식으로 반환한다
        return String.join("^", authors);
    }

    /**
     * 카카오 ISBN 문자열에서 기존 데이터와 호환되는 ISBN13을 우선 선택한다
     *
     * @author SeungHyeon.Kang
     * @param isbn 공백으로 구분된 ISBN10 또는 ISBN13 문자열
     * @return ISBN13 우선 식별값 또는 빈 문자열
     */
    private String getIsbn(String isbn) {
        // ISBN이 없는 도서도 화면에서 안전하게 검증할 수 있도록 빈 문자열을 적용한다
        if (StringUtil.isEmpty(isbn) || isbn.isBlank()) {
            // 비어 있는 ISBN을 반환한다
            return StringUtil.EMPTY;
        }

        // ISBN10과 ISBN13을 개별 후보로 분리한다
        String[] isbnValues = isbn.trim().split("\\s+");
        // 카카오 응답에서 마지막에 제공되는 ISBN13을 우선 반환한다
        return isbnValues[isbnValues.length - 1];
    }

    /**
     * 카카오 출간일시를 기존 화면의 yyyyMMdd 형식으로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param datetime ISO 8601 형식의 도서 출간일시
     * @return yyyyMMdd 형식의 출간일 또는 빈 문자열
     */
    private String getPublishedDate(String datetime) {
        // 출간일이 없는 도서도 화면에서 안전하게 렌더링할 수 있도록 빈 문자열을 적용한다
        if (StringUtil.isEmpty(datetime)) {
            // 비어 있는 출간일을 반환한다
            return StringUtil.EMPTY;
        }

        // ISO 8601 날짜 부분만 yyyyMMdd 형식으로 변환한다
        int dateEndIndex = Math.min(datetime.length(), 10);
        // 기존 화면과 저장 로직이 사용하는 출간일 형식으로 반환한다
        return datetime.substring(0, dateEndIndex).replace("-", StringUtil.EMPTY);
    }

    /**
     * 외부 API 문자열의 null을 기존 화면 계약의 빈 문자열로 보정한다
     *
     * @author SeungHyeon.Kang
     * @param text 카카오 도서 검색 응답 문자열
     * @return null이 제거된 화면 문자열
     */
    private String getSafeText(String text) {
        // null 외부 문자열은 화면에서 직접 참조할 수 있도록 빈 문자열로 반환한다
        return StringUtil.isEmpty(text) ? StringUtil.EMPTY : text;
    }
}
