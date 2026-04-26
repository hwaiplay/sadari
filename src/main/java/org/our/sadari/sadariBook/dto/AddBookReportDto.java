package org.our.sadari.sadariBook.dto;

import lombok.Data;

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
public class AddBookReportDto {
    // "책" 관련 ---------------
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

    // "독후감" 관련 ---------------
    // 독서 상태(완독/읽는중/중단)
    private String status;
    // 독서 시작일
    private String startDate;
    // 독서 종료일
    private String endDate;
    // 별점
    private String grade;
    // 독후감 내용
    private String content;

    public AddBookReportDto(
        String title,
        String author,
        String publisher,
        String isbn,
        String image,
        String description,
        String status,
        String startDate,
        String endDate,
        String grade,
        String content
    ) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.isbn = isbn;
        this.image = image;
        this.description = description;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.grade = grade;
        this.content = content;
    }
}