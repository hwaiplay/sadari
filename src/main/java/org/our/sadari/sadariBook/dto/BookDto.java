package org.our.sadari.sadariBook.dto;

import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * packageName    : org.our.sadari.sadariBook.dto
 * fileName       : BookDto.java
 * author         : hanwon.Jang
 * date           : 2026-04-09
 * description    : 책 DTO
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
    @Size(max = 500)
    private String bookTitl;
    // 저자
    @Size(max = 500)
    private String bookAthr;
    // 출판사
    @Size(max = 500)
    private String bookPubl;
    // Isbn
    @Size(max = 100)
    private String bookIsbn;
    // 책 표지 이미지
    @Size(max = 1000)
    private String bookCvim;
    // 책 소개 내용
    @Size(max = 4000)
    private String bookDesc;
    // 공개여부와 상관없이 전체 독후감 기준으로 계산한 도서 평균 별점
    private BigDecimal bookAvgGrde;

}
