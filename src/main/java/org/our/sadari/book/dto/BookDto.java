package org.our.sadari.book.dto;

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
public class BookDto {

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private Long bookNumb;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Size(max = 500)
    private String bookTitl;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Size(max = 500)
    private String bookAthr;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Size(max = 500)
    private String bookPubl;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Size(max = 100)
    private String bookIsbn;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Size(max = 1000)
    private String bookCvim;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Size(max = 4000)
    private String bookDesc;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private String publDate;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private BigDecimal bookAvgGrde;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private String locale;
}
