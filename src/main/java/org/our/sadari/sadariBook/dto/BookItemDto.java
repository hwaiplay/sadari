package org.our.sadari.sadariBook.dto;

import lombok.Data;

/**
 * packageName    : org.our.sadari.sadariBook.dto
 * fileName       : BookItemDto.java
 * author         : hanwon.Jang
 * date           : 2026-04-08
 * description    : 책 검색 결과 해당 책의 정보를 담는 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-08       hanwon.Jang        주석 추가
 */

@Data
public class BookItemDto {
    private String title;
    private String author;
    private String publisher;
    private String isbn;
    private String image;
    private String description;
}
