package org.our.sadari.sadariBook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * packageName    : org.our.sadari.sadariBook.dto
 * fileName       : HomeBookDto.java
 * author         : hanwon.Jang
 * date           : 2026-04-26
 * description    : 홈 화면의 독후감 리스트에서 사용되는 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-26       hanwon.Jang       최초 생성
 */

@Data
@AllArgsConstructor
public class HomeBookDto {
    // 책 번호
    private Long bookNumb; 
    // 유저 번호
    private Long userNumb; 
    // 책 제목
    private String bookTitl; 
}