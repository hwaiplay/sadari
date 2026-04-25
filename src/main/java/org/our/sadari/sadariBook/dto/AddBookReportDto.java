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
    private String title;
    private String author;
    private String publisher;
    private String isbn;
    private String image;
    private String description;

    private String status;
    private String startDate;
    private String endDate;
    private String grade;
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