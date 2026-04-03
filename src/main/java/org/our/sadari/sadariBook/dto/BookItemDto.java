package org.our.sadari.sadariBook.dto;

import lombok.Data;

@Data
public class BookItemDto {
    private String title;
    private String author;
    private String publisher;
    private String isbn;
    private String image;
    private String description;
}
