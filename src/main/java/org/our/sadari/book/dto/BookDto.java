package org.our.sadari.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BookDto 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "도서 정보 DTO")
public class BookDto {

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Schema(description = "도서 번호", example = "1")
    private Long bookNumb;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Schema(description = "도서 제목", example = "용의자 X의 헌신")
    @Size(max = 500)
    private String bookTitl;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Schema(description = "저자명", example = "히가시노 게이고")
    @Size(max = 500)
    private String bookAthr;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Schema(description = "출판사", example = "재인")
    @Size(max = 500)
    private String bookPubl;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Schema(description = "ISBN", example = "9788990982704")
    @Size(max = 100)
    private String bookIsbn;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Schema(description = "도서 표지 이미지 URL")
    @Size(max = 1000)
    private String bookCvim;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Schema(description = "도서 설명")
    @Size(max = 4000)
    private String bookDesc;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Schema(description = "출간일", example = "2006-08-11")
    private String publDate;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Schema(description = "도서 평균 평점", example = "4.5")
    private BigDecimal bookAvgGrde;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Schema(description = "도서 검색 API locale 값")
    private String locale;
}
