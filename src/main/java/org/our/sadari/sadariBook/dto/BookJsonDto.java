package org.our.sadari.sadariBook.dto;

import java.util.List;

import lombok.Data;

/**
 * packageName    : package org.our.sadari.sadariBook.dto;
 * fileName       : BookJsonDto.java
 * author         : hanwon.Jang
 * date           : 2026-04-02
 * description    : 네이버 API 응답을 담는 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-02       hanwon.Jang       최초 생성
 */

@Data
public class BookJsonDto {
    // 검색 결과를 생성한 시간
    private String lastBuildDate;
    // 검색 결과 수
    private int total;
    // 검색 시작 위치(기본값: 1, 최댓값: 1000) 
    private int start;
    // 한 번에 표시할 검색 결과 개수(기본값: 10, 최댓값: 100)
    private int display;
    // 검색된 책 데이터륻 담는 배열
    private List<BookDto> items;

    @Data
    public class BookDto {
        // 책 제목
        private String title;
        // 저자
        private String author;
        // 출판사
        private String publisher; 
        // isbn
        private String isbn; 
        // 책 표지 이미지
        private String image; 
        // 책 소개 내용
        private String description; 
    }
}
