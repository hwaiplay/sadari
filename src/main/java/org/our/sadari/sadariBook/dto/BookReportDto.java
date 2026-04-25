package org.our.sadari.sadariBook.dto;

import org.our.sadari.sadariBook.entity.BookReportEntity;
import lombok.Data;

/**
 * packageName    : org.our.sadari.sadariBook.dto
 * fileName       : BookReportDto.java
 * author         : hanwon.Jang
 * date           : 2026-04-03
 * description    : 유저가 작성한 독후감 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-03       hanwon.Jang       최초 생성
 */

@Data
public class BookReportDto {
    private String status;
    private String startDate;
    private String endDate;
    private String grade;
    private String content;

    public static BookReportDto from(BookReportEntity entity) {
        BookReportDto dto = new BookReportDto();
        dto.setStatus(entity.getBookStat());
        dto.setStartDate(entity.getBookStdt());
        dto.setEndDate(entity.getBookEndt());
        dto.setGrade(entity.getBookGrde());
        dto.setContent(entity.getBookCntn());

        return dto;
    }
}
