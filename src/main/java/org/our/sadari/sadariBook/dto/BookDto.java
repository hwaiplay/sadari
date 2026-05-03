package org.our.sadari.sadariBook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * packageName    : org.our.sadari.sadariBook.dto
 * fileName       : AddBookReportDto.java
 * author         : hanwon.Jang
 * date           : 2026-04-09
 * description    : 독후감 등록 요청 데이터 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-09       hanwon.Jang       최초 생성
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDto {
    // "책" 관련 필드 -----------
    // 책 번호
    private Long bookNumb;
    // 책 제목
    private String bookTitl;
    // 저자
    private String bookAthr;
    // 출판사
    private String bookPubl;
    // Isbn
    private String bookIsbn;
    // 책 표지 이미지
    private String bookCvim;
    // 책 소개 내용
    private String bookDesc;

}