package org.our.sadari.sadariBook.dto;

import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 책 기본 정보를 전달하는 DTO입니다.
 *
 * @author Seunghyeon.Kang
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDto {

    /** 책 테이블의 PK 번호입니다. */
    private Long bookNumb;

    /** 책 제목입니다. */
    @Size(max = 500)
    private String bookTitl;

    /** 작가 이름입니다. */
    @Size(max = 500)
    private String bookAthr;

    /** 출판사 이름입니다. */
    @Size(max = 500)
    private String bookPubl;

    /** ISBN 값입니다. */
    @Size(max = 100)
    private String bookIsbn;

    /** 책 표지 이미지 URL입니다. */
    @Size(max = 1000)
    private String bookCvim;

    /** 책 소개 내용입니다. */
    @Size(max = 4000)
    private String bookDesc;

    /** 출간일입니다. */
    private String publDate;

    /** 공개 여부와 상관없이 해당 책 전체 독후감 기준으로 계산한 평균 별점입니다. */
    private BigDecimal bookAvgGrde;
}
