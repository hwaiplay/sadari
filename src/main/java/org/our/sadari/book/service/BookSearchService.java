package org.our.sadari.book.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.our.sadari.book.dto.BookJsonDto;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * fileName       : BookSearchService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-06
 * description    : 도서 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-06        SeungHyeon.Kang    최초 생성
 */
@Service
@RequiredArgsConstructor
public class BookSearchService {

    // 네이버 도서 검색 URL 설정값
    private static final String NAVER_BOOK_SEARCH_URL = "https://openapi.naver.com/v1/search/book.json";
    // 표시 건수 설정값
    private static final int DISPLAY_COUNT = 10;
    // 최소 시작 설정값
    private static final int MIN_START = 1;
    // 최대 시작 설정값
    private static final int MAX_START = 1000;

    // 네이버 도서 검색 API 클라이언트 식별자
    @Value("${naver.key.clientId}")
    private String naverClientId;

    // 네이버 도서 검색 API 클라이언트 비밀값
    @Value("${naver.key.clientSecret}")
    private String naverClientSecret;

    // 외부 HTTP API 통신 객체
    private final RestTemplate restTemplate;
    // Object 데이터 접근 객체
    private final ObjectMapper objectMapper;

    /**
     * 검색어 기준 네이버 도서 목록 검색한다.
     *
     * @author SeungHyeon.Kang
     * @param query 네이버 도서 API에 전달할 검색어
     * @param start 네이버 도서 검색 결과의 시작 위치
     * @return 처리 결과
     */
    public ResultData searchBooks(String query, int start) {

        // query 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (StringUtil.isEmpty(query) || start < MIN_START || start > MAX_START) {

            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 사용자 검색어로 네이버 도서 검색 API를 호출한다
        ResponseEntity<String> response = requestNaverBookSearch(query, start);

        // response.getBody( 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (StringUtil.isEmpty(response.getBody())) {

            // "검색에 실패했어요.\n다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_SEARCH_REJECTED);
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // 외부 API의 JSON 응답을 업무 DTO로 변환한다
            BookJsonDto bookJsonDto = objectMapper.readValue(response.getBody(), BookJsonDto.class);
            // 검색어 기준 네이버 도서 목록 검색 결과를 성공 응답으로 반환한다
            return ResultData.success(bookJsonDto.getItems());
        }
        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (JsonProcessingException e) {

            // 아래 처리 단계의 업무 목적을 설명한다.
            // "검색에 실패했어요.\n다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_SEARCH_REJECTED);
        }
    }

    /**
     * 네이버 도서 API 검색 요청한다.
     *
     * @author SeungHyeon.Kang
     * @param query 네이버 도서 API에 전달할 검색어
     * @param start 네이버 도서 검색 결과의 시작 위치
     * @return 처리 결과
     */
    private ResponseEntity<String> requestNaverBookSearch(String query, int start) {

        // 외부 API 요청 헤더를 담을 객체를 생성한다
        HttpHeaders headers = new HttpHeaders();
        // 처리한 값을 결과 컬렉션에 추가한다
        headers.add("X-Naver-Client-Id", naverClientId);
        // 처리한 값을 결과 컬렉션에 추가한다
        headers.add("X-Naver-Client-Secret", naverClientSecret);

        URI uri = UriComponentsBuilder
                .fromUriString(NAVER_BOOK_SEARCH_URL)
                .queryParam("query", query)
                .queryParam("display", DISPLAY_COUNT)
                .queryParam("start", start)
                .queryParam("sort", "sim")
                .build()
                .encode()
                .toUri();

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // 네이버 도서 API 검색 요청 결과를 반환한다
            return restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );
        }
        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (RestClientException e) {

            // HTTP 응답 상태와 본문을 반환한다
            return ResponseEntity.ok("");
        }
    }
}
