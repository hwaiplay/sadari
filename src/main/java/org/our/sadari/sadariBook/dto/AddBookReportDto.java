package org.our.sadari.sadariBook.dto;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
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
}