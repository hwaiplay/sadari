package org.our.sadari.book.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * fileName       : KakaoBookJsonDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-02
 * description    : 카카오 도서 검색 API 원문 응답을 역직렬화한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-02        SeungHyeon.Kang    최초 생성
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "카카오 도서 검색 API 원문 응답 DTO", hidden = true)
public class KakaoBookJsonDto {

    // 카카오 도서 검색 API의 documents 목록
    private List<BookDto> documents;

    /**
     * 카카오 도서 검색 API가 반환하는 개별 도서 원문 필드를 정의한다
     *
     * @author SeungHyeon.Kang
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "카카오 도서 검색 API 원문 항목 DTO", hidden = true)
    public static class BookDto {

        // 카카오 도서 제목
        private String title;

        // 카카오 도서 저자 목록
        private List<String> authors;

        // 카카오 도서 출판사
        private String publisher;

        // 카카오 도서 ISBN10과 ISBN13 문자열
        private String isbn;

        // 카카오 도서 표지 미리보기 URL
        private String thumbnail;

        // 카카오 도서 소개
        private String contents;

        // 카카오 도서 출간일시
        private String datetime;
    }
}
