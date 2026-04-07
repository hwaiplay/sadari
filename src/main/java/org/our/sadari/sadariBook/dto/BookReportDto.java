package org.our.sadari.sadariBook.dto;

import lombok.Data;

/**
 * packageName    : 
 * fileName       : BookReportDto.java
 * author         : hanwon.Jang
 * date           : 2026-04-03
 * description    : 독후감 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-03       hanwon.Jang       최초 생성
 */

@Data
public class BookReportDto {
    private String coverImage;
    private String status;
    private String startDate;
    private String endDate;
    private String grade;
    private String content;
}
