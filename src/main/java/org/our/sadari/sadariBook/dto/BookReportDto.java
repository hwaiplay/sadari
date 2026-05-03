package org.our.sadari.sadariBook.dto;

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
 * 2026-04-03       hanwon.Jang       독후감 번호, 책 번호 추가
 */

@Data
public class BookReportDto extends BookDto {
    //유저 번호
    private Long UserNumb;
    // 독후감 번호 
    private Long reportNumb; 
    // 책 번호
    private Long bookNumb; 
    // 독서 상태(완독/읽는중/중단)
    private String reportStat;
    // 독서 시작일
    private String reportStdt;
    // 독서 종료일
    private String reportEndt;
    // 별점
    private String reportGrde;
    // 독후감 내용
    private String reportCntn;
    
}
