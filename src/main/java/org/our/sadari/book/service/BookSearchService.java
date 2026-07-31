package org.our.sadari.book.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookSearchService {

    // 표시 건수 설정값
    private static final int DISPLAY_COUNT = 10;
    // 최소 시작 설정값
    private static final int MIN_START = 1;
    // 카카오 도서 검색의 최대 50페이지를 기존 시작 위치 계약으로 환산한 설정값
    private static final int MAX_START = 491;
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

    /**
     * 검색어 기준 카카오 도서 목록을 검색한다
     *
     * @author SeungHyeon.Kang
     * @param query 카카오 도서 API에 전달할 검색어
     * @param start 기존 화면 계약에서 사용하는 검색 결과 시작 위치
     * @return 사용자 화면 형식으로 변환된 도서 검색 결과
     */
    public ResultData searchBooks(String query, int start) {
        // 비어 있는 검색어나 카카오 API 범위를 벗어난 시작 위치는 외부 요청 전에 차단한다
        if (StringUtil.isEmpty(query) || start < MIN_START || start > MAX_START) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 카카오 API 통신과 응답 변환 실패를 공통 검색 실패 응답으로 격리한다
        try {
            // 사용자 검색어로 카카오 도서 검색 API를 호출한다
            ResponseEntity<String> response = requestKakaoBookSearch(query, start);

            // 본문이 없는 외부 응답은 정상 검색 결과로 해석하지 않는다
            if (StringUtil.isEmpty(response.getBody())) {
                // "검색에 실패했어요.\n다시 시도해주세요."
                return ResultData.fail(ResultEnum.COMMON_SEARCH_REJECTED);
            }

            // 카카오 JSON 응답을 기존 사용자 화면 계약의 도서 목록으로 변환한다
            BookJsonDto bookJsonDto = objectMapper.readValue(response.getBody(), BookJsonDto.class);
            // 검색어 기준 카카오 도서 목록을 성공 응답으로 반환한다
            return ResultData.success(bookJsonDto.getItems());
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
     * 기존 시작 위치를 카카오 페이지 번호로 변환하여 도서 검색 API를 호출한다
     *
     * @author SeungHyeon.Kang
     * @param query 카카오 도서 API에 전달할 검색어
     * @param start 기존 화면 계약에서 사용하는 검색 결과 시작 위치
     * @return 카카오 도서 검색 API의 HTTP 응답
     */
    private ResponseEntity<String> requestKakaoBookSearch(String query, int start) {
        // 기존 시작 위치를 카카오 API의 1부터 시작하는 페이지 번호로 변환한다
        int page = ((start - MIN_START) / DISPLAY_COUNT) + 1;
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
}
