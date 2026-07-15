package org.our.sadari.sadariBook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Naver 책 검색 API 응답을 매핑하는 DTO입니다.
 *
 * @author Seunghyeon.Kang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookJsonDto {

    /** Naver 검색 결과가 생성된 일시입니다. */
    private String lastBuildDate;

    /** 검색 결과 전체 건수입니다. */
    private int total;

    /** 검색 결과 시작 위치입니다. */
    private int start;

    /** 한 번에 표시되는 검색 결과 수입니다. */
    private int display;

    /** 검색된 책 목록입니다. */
    private List<BookDto> items;

    /**
     * Naver 책 검색 API의 단건 책 정보를 매핑하는 DTO입니다.
     *
     * @author Seunghyeon.Kang
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookDto {

        /** 책 제목입니다. */
        private String title;

        /** 작가 이름입니다. */
        private String author;

        /** 출판사 이름입니다. */
        private String publisher;

        /** ISBN 값입니다. */
        private String isbn;

        /** 책 표지 이미지 URL입니다. */
        private String image;

        /** 책 소개 내용입니다. */
        private String description;

        /** 출간일입니다. Naver API는 yyyyMMdd 형식으로 내려줍니다. */
        private String pubdate;
    }
}
