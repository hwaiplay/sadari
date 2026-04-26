package org.our.sadari.sadariBook.dto;

import org.our.sadari.sadariBook.entity.BookEntity;
import lombok.Data;

/**
 * packageName    : org.our.sadari.sadariBook.dto
 * fileName       : BookDto.java
 * author         : hanwon.Jang
 * date           : 2026-04-08
 * description    : "책"의 정보를 담는 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-08       hanwon.Jang        주석 추가
 * 2026-04-22       hanwon.Jang        set/get 추가
 */

@Data
public class BookDto {
    // 책 제목
    private String title;
    // 저자
    private String author;
    // 출판사
    private String publisher; 
    // isbn
    private String isbn; 
    // 책 표지 이미지
    private String image; 
    // 책 소개 내용
    private String description; 

    public static BookDto from(BookEntity entity) {
        BookDto dto = new BookDto();
        dto.setTitle(entity.getBookTitl());
        dto.setAuthor(entity.getBookAthr());
        dto.setPublisher(entity.getBookPubl());
        dto.setIsbn(entity.getBookIsbn());
        dto.setImage(entity.getBookCvim());
        dto.setDescription(entity.getBookDesc());
        
        return dto;
    }
}
