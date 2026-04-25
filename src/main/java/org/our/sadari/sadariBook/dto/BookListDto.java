package org.our.sadari.sadariBook.dto;

import org.our.sadari.sadariBook.entity.BookEntity;

import lombok.Builder;
import lombok.Getter;

/**
 * packageName    : org.our.sadari.sadariBook.dto
 * fileName       : BookListDto.java
 * author         : hanwon.Jang
 * date           : 2026-04-09
 * description    : 독후감 리스트 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-09       hanwon.Jang       최초 생성
 */

@Getter
@Builder
public class BookListDto {
    private Long id;
    private String title;

    public static BookListDto from(BookEntity entity) {
        return BookListDto.builder()
            .id(entity.getBookNumb())
            .title(entity.getBookTitl())
            .build();
    }
}
