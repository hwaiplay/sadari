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
 * BookSearchService 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
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
     * searchBooks 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param query 처리에 필요한 입력값
     * @param start 처리에 필요한 입력값
     * @return 처리 결과
     */
    public ResultData searchBooks(String query, int start) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (StringUtil.isEmpty(query) || start < MIN_START || start > MAX_START) {
            // 호출한 계층에서 사용할 처리 결과를 반환한다.
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        ResponseEntity<String> response = requestNaverBookSearch(query, start);

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (StringUtil.isEmpty(response.getBody())) {
            // 호출한 계층에서 사용할 처리 결과를 반환한다.
            return ResultData.fail(ResultEnum.COMMON_SEARCH_REJECTED);
        }

        try {
            BookJsonDto bookJsonDto = objectMapper.readValue(response.getBody(), BookJsonDto.class);
            return ResultData.success(bookJsonDto.getItems());
        } catch (JsonProcessingException e) {
            // 아래 처리 단계의 업무 목적을 설명한다.
            // 호출한 계층에서 사용할 처리 결과를 반환한다.
            return ResultData.fail(ResultEnum.COMMON_SEARCH_REJECTED);
        }
    }

    /**
     * requestNaverBookSearch 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param query 처리에 필요한 입력값
     * @param start 처리에 필요한 입력값
     * @return 처리 결과
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
            // 호출한 계층에서 사용할 처리 결과를 반환한다.
            return ResponseEntity.ok("");
        }
    }
}
