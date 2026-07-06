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

@Slf4j
@Service
@RequiredArgsConstructor
public class BookSearchService {

    // 네이버 책 검색 주소는 검색 서비스 안에서만 관리한다.
    private static final String NAVER_BOOK_SEARCH_URL = "https://openapi.naver.com/v1/search/book.json";

    // 네이버 인증 값은 설정 파일에서 주입받는다.
    @Value("${naver.key.clientId}")
    private String naverClientId;

    @Value("${naver.key.clientSecret}")
    private String naverClientSecret;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public List<BookJsonDto.BookDto> searchBooks(String query) {
        // 검색어가 비어 있으면 외부 요청을 보내지 않고 요청 오류로 처리한다.
        if (StringUtil.isEmpty(query)) {
            throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }

        // 외부 호출과 응답 변환을 서비스 내부에서 처리해 컨트롤러 책임을 줄인다.
        ResponseEntity<String> response = requestNaverBookSearch(query);
        if (StringUtil.isEmpty(response.getBody())) {
            throw new CustomException(ResultEnum.COMMON_SEARCH_REJECTED, HttpStatus.BAD_GATEWAY);
        }

        try {
            BookJsonDto bookJsonDto = objectMapper.readValue(response.getBody(), BookJsonDto.class);
            return bookJsonDto.getItems();
        } catch (JsonProcessingException e) {
            // 응답 형식이 예상과 다르면 검색 실패로 변환한다.
            log.warn("Failed to parse Naver book search response. query={}", query, e);
            throw new CustomException(ResultEnum.COMMON_SEARCH_REJECTED, HttpStatus.BAD_GATEWAY);
        }
    }

    private ResponseEntity<String> requestNaverBookSearch(String query) {
        // 네이버 API 인증 헤더는 호출 직전에 구성한다.
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Naver-Client-Id", naverClientId);
        headers.add("X-Naver-Client-Secret", naverClientSecret);

        // 검색어는 주소 조립 과정에서 인코딩되도록 처리한다.
        URI uri = UriComponentsBuilder
                .fromUriString(NAVER_BOOK_SEARCH_URL)
                .queryParam("query", query)
                .queryParam("display", 10)
                .queryParam("sort", "sim")
                .build()
                .encode()
                .toUri();

        try {
            // RestTemplate 빈을 사용해 외부 API 호출 객체 생성을 반복하지 않는다.
            return restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );
        } catch (RestClientException e) {
            // 네이버 API 호출 실패는 공통 검색 실패 응답으로 변환한다.
            log.warn("Failed to call Naver book search API. query={}", query, e);
            throw new CustomException(ResultEnum.COMMON_SEARCH_REJECTED, HttpStatus.BAD_GATEWAY);
        }
    }
}
