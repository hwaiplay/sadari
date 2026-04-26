package org.our.sadari.sadariBook.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.sadariBook.dto.AddBookReportDto;
import org.our.sadari.sadariBook.dto.BookDto;
import org.our.sadari.sadariBook.dto.BookJsonDto;
import org.our.sadari.sadariBook.dto.HomeBookDto;
import org.our.sadari.sadariBook.repository.BookReportRepository;
import org.our.sadari.sadariBook.service.BookServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * packageName    : org.our.sadari.sadariBook.controller
 * fileName       : BookController.java
 * author         : hanwon.Jang
 * date           : 2026-04-01
 * description    : 독후감 관련 컨트롤러    
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-01       hanwon.Jang       최초 생성
 * 2026-04-23       hanwon.Jang       독후감 상세보기 로직
 * 2026-04-25       hanwon.Jang       독후감 리스트 조회 로직
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book")
public class BookController {

    @Value("${naver.key.clientId}")
    private String NAVER_CLIENT_ID; //네이버 앱 클라이언트 키

    @Value("${naver.key.clientSecret}")
    private String NAVER_CLIENT_SECRET; //네이버 앱 시크릿 키

    private final BookServiceImpl bookServiceImpl;
    private final BookReportRepository bookReportRepository;

    private StringUtil stringUtil;

    /**
     *  책 검색 Api
     */
    @GetMapping("/search")
    public ResultData<?> searchBooks(@RequestParam String query) throws JsonProcessingException {

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

        List<BookDto> books = bookJsonDto.getItems();

        return ResultData.success(books);

    }
    
    /**
     * 독후감 기록 Api
     */
    @PostMapping("/addBookReport")
    public ResultData<?> createReport(@RequestBody AddBookReportDto addBookReportDto) {

        //if(stringUtil.isEmpty(addBookReportDto.getAuthor() || ))

        if(stringUtil.isEmpty(addBookReportDto)) {
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        Long bookId = bookServiceImpl.createReport(addBookReportDto);
        
        log.debug("독후감 기록 성공: " + addBookReportDto);

        return ResultData.success(bookId);
    }

    @GetMapping("/getBookdetail/{id}")
    public ResponseEntity<ResultData<?>> getDetail(@PathVariable Long id) {

        if(id == null) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResultData.fail(ResultEnum.AUTH_FAIL));
        }

        AddBookReportDto detail = bookServiceImpl.getDetail(id);

        log.debug("독후감 상세보기 조회 성공: " + detail);

        return ResponseEntity.ok(ResultData.success(detail));
    }

    /**
     * 독후감 리스트 조회 API
     * @param userNumb 유저번호
     * @return 리스트
     */
    @GetMapping("/getBookList/{userNumb}")
    public ResponseEntity<ResultData<?>> getBookList(@PathVariable Long userNumb) {

        List<HomeBookDto> list = bookServiceImpl.getBookList(userNumb);

        return ResponseEntity.ok(ResultData.success(list));
    }
}
