package org.our.sadari.book.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BookJsonDto 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookJsonDto {

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private String lastBuildDate;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int total;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int start;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private int display;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private List<BookDto> items;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookDto {

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
        private String title;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
        private String author;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
        private String publisher;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
        private String isbn;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
        private String image;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
        private String description;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
        private String pubdate;
    }
}
