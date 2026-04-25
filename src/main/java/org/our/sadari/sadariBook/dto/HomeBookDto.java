package org.our.sadari.sadariBook.dto;

import lombok.Data;

/**
 * packageName    : org.our.sadari.sadariBook.dto
 * fileName       : HomeBookDto.java
 * author         : hanwon.Jang
 * date           : 2026-04-26
 * description    : 홈화면의 독후감 리스트에서 사용되는 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-26       hanwon.Jang       최초 생성
 */

@Data
public class HomeBookDto {
    private Long bookNumb;
    private Long reportNumb;
    private String bookTitle;

    public HomeBookDto(Long bookNumb, Long reportNumb, String bookTitle) {
        this.bookNumb = bookNumb;
        this.reportNumb = reportNumb;
        this.bookTitle = bookTitle;
    }
}