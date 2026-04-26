package org.our.sadari.sadariBook.dto;

import org.our.sadari.sadariBook.entity.BookReportEntity;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class BookReportDto {
    // 독후감 번호 
    private Long reportNumb; 
    // 책 번호
    private Long bookNumb; 
    // 독서 상태(완독/읽는중/중단)
    private String bookStat;
    // 독서 시작일
    private String bookStdt;
    // 독서 종료일
    private String bookEndt;
    // 별점
    private String bookGrde;
    // 독후감 내용
    private String bookCntn;
    
}
