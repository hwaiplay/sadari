package org.our.sadari.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * fileName       : BookJsonDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 도서 요청과 응답 데이터를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    DTO 문서화 규칙 정비
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "네이버 도서 검색 API 응답 DTO", hidden = true)
public class BookJsonDto {
    // 네이버 도서 검색 API의 검색 결과 생성 일시
    private String lastBuildDate;

    // 검색어에 해당하는 전체 도서 검색 결과 건수
    private int total;

    // 현재 응답의 도서 검색 시작 위치
    private int start;

    // 현재 응답에 포함된 도서 검색 결과 건수
    private int display;

    // 네이버 도서 검색 API에서 조회된 도서 목록
    private List<BookDto> items;

    /**
     * 네이버 도서 검색 API가 반환한 개별 도서 정보를 전달한다
     *
     * @author SeungHyeon.Kang
     */
    // 네이버 도서 검색 API에서 조회된 개별 도서 정보
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "네이버 도서 검색 결과 항목 DTO", hidden = true)
    public static class BookDto {
        // 도서 제목
        private String title;

        // 도서를 집필한 저자명
        private String author;

        // 도서를 발행한 출판사명
        private String publisher;

        // 도서를 식별하는 ISBN 값
        private String isbn;

        // 네이버에서 제공하는 도서 표지 이미지 URL
        private String image;

        // 도서의 주요 내용을 요약한 설명
        private String description;

        // yyyyMMdd 형식의 도서 출간일
        private String pubdate;
    }
}
