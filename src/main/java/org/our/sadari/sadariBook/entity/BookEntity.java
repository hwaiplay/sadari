package org.our.sadari.sadariBook.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * packageName    : org.our.sadari.sadariBook.entity
 * fileName       : BookEntity.java
 * author         : hanwon.Jang
 * date           : 2026-04-03
 * description    : '독후감이 기록된 책'을 정의한 Entity
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-03       hanwon.Jang       최초 생성
 */

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TM_BOOK_INFO")
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tm_book_info_seq_gen")
    @SequenceGenerator(
        name = "tm_book_info_seq_gen",
        sequenceName = "TM_BOOK_INFO_SEQ",
        allocationSize = 1
    )

    // 책 번호
    @Column(name = "BOOK_NUMB")
    private Long bookNumb;
    
    // 책 제목
    @Column(name = "BOOK_TITL", nullable = false)
    private String bookTitl;
    
    // 저자
    @Column(name = "BOOK_ATHR", nullable = false)
    private String bookAthr;
    
    // 출판사
    @Column(name = "BOOK_PUBL", nullable = false)
    private String bookPubl;
    
    // isbn
    @Column(name = "BOOK_ISBN", nullable = false)
    private String bookIsbn;

    // 책 표지 이미지
    @Column(name = "BOOK_CVIM", nullable = false)
    private String bookCvim;
    
    // 책 설명
    @Column(name = "BOOK_DESC", length = 4000, nullable = false)
    private String bookDesc;
}