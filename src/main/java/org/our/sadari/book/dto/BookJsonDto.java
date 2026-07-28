package org.our.sadari.book.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BookJsonDto 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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

    // 네이버 도서 검색 API에서 조회된 개별 도서 정보
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
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
