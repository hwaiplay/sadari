package org.our.sadari.sadariBook.dto;

import java.util.List;

import lombok.Data;

/**
 * packageName    : 
 * fileName       : BookJsonDto.java
 * author         : hanwon.Jang
 * date           : 2026-04-02
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-02       hanwon.Jang       최초 생성
 */

@Data
public class BookJsonDto {
    private String lastBuildDate;
    private int total;
    private int start;
    private int display;
    private List<BookItemDto> items;
}
