package org.our.sadari.sadariBook.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.exception.CustomException;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.sadariBook.dto.BookJsonDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Naver 책 검색 API 호출과 응답 변환을 담당하는 서비스입니다.
 *
 * @author Seunghyeon.Kang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookSearchService {

    private static final String NAVER_BOOK_SEARCH_URL = "https://openapi.naver.com/v1/search/book.json";
    private static final int DISPLAY_COUNT = 10;
    private static final int MIN_START = 1;
    private static final int MAX_START = 1000;

    @Value("${naver.key.clientId}")
    private String naverClientId;

    @Value("${naver.key.clientSecret}")
    private String naverClientSecret;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 검색어와 시작 위치로 Naver 책 검색 API를 호출하고 책 목록을 반환합니다.
     *
     * @author Seunghyeon.Kang
     * @param query 책 검색어
     * @param start 검색 시작 위치
     * @return 검색된 책 목록
     */
    public List<BookJsonDto.BookDto> searchBooks(String query, int start) {
        // Naver 책 검색 API의 start는 1부터 1000까지만 허용되므로 API 호출 전에 차단합니다.
        if (StringUtil.isEmpty(query) || start < MIN_START || start > MAX_START) {
            throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<String> response = requestNaverBookSearch(query, start);
        // 외부 API 응답 본문이 비어 있으면 정상 검색 결과로 볼 수 없으므로 검색 실패로 처리합니다.
        if (StringUtil.isEmpty(response.getBody())) {
            throw new CustomException(ResultEnum.COMMON_SEARCH_REJECTED, HttpStatus.BAD_GATEWAY);
        }

        try {
            BookJsonDto bookJsonDto = objectMapper.readValue(response.getBody(), BookJsonDto.class);
            return bookJsonDto.getItems();
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse Naver book search response. query={}", query, e);
            throw new CustomException(ResultEnum.COMMON_SEARCH_REJECTED, HttpStatus.BAD_GATEWAY);
        }
    }

    /**
     * Naver 책 검색 API에 HTTP 요청을 전송합니다.
     *
     * @author Seunghyeon.Kang
     * @param query 책 검색어
     * @param start 검색 시작 위치
     * @return Naver API 원문 응답
     */
    private ResponseEntity<String> requestNaverBookSearch(String query, int start) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Naver-Client-Id", naverClientId);
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

        try {
            return restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );
        } catch (RestClientException e) {
            log.warn("Failed to call Naver book search API. query={}", query, e);
            throw new CustomException(ResultEnum.COMMON_SEARCH_REJECTED, HttpStatus.BAD_GATEWAY);
        }
    }
}
