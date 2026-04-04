package org.our.sadari.sadariBook.controller;

import java.util.List;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.sadariBook.dto.BookItemDto;
import org.our.sadari.sadariBook.dto.BookJsonDto;
import org.our.sadari.sadariBook.dto.BookReportDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

/**
 * packageName    : org.our.sadari.sadariBook.controller
 * fileName       : BookController.java
 * author         : hanwon.Jang
 * date           : 2026-04-01
 * description    : 네이버 책 검색 API 연동 컨트롤러    
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-01       hanwon.Jang       최초 생성
 */

@Slf4j
@RestController
@RequestMapping("/api/book")
public class BookController {

    @Value("${naver.key.clientId}")
    private String NAVER_CLIENT_ID; //네이버 앱 클라이언트 키

    @Value("${naver.key.clientSecret}")
    private String NAVER_CLIENT_SECRET; //네이버 앱 시크릿 키

    /**
     *  책 검색 Api
     */
    @GetMapping("/search")
    public ResponseEntity<ResultData<?>> searchBooks(@RequestParam String query) throws JsonProcessingException {

        // HTTP 요청을 보내기 위한 RestTemplate 객체 생성
        RestTemplate rt = new RestTemplate();

        // HTTP Header 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Naver-Client-Id", NAVER_CLIENT_ID);
        headers.add("X-Naver-Client-Secret", NAVER_CLIENT_SECRET);

        String url = "https://openapi.naver.com/v1/search/book.json?query=" + query + "&display=10&sort=sim";

        ResponseEntity<String> response = rt.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
        );

        // JSON 응답 문자열을 KakaoTokenVO 객체로 역직렬화(Parsing)
        ObjectMapper objectMapper = new ObjectMapper();
            
        BookJsonDto bookJsonDto = objectMapper.readValue(response.getBody(), BookJsonDto.class);

        List<BookItemDto> books = bookJsonDto.getItems();

        return ResponseEntity.ok(ResultData.success(books));

    }
    
    /**
     * 독후감 기록 Api
     */
    @PostMapping("/addBookReport")
    public ResponseEntity<ResultData<?>> createReport(@RequestBody BookReportDto request) {
        log.debug("독후감 기록: " + request);

        if(request == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResultData.fail(ResultEnum.AUTH_FAIL));
        }

        return ResponseEntity.ok(ResultData.success());
    }
}
