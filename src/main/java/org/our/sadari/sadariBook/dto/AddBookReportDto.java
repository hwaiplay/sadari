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
    
    // "독후감" 관련 필드 ---------
    // 독후감 번호
    private Long reportNumb;
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