package org.our.sadari.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.our.sadari.global.common.util.StringUtil;

/**
 * fileName       : BookJsonDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 카카오 도서 검색 응답을 사용자 화면 계약으로 변환한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    DTO 문서화 규칙 정비
 * 2026-07-31        SeungHyeon.Kang    카카오 도서 검색 응답 형식 적용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "카카오 도서 검색 API 응답 DTO", hidden = true)
public class BookJsonDto {

    // 카카오 도서 검색 API에서 조회된 도서 목록
    @JsonAlias("documents")
    private List<BookDto> items;

    /**
     * 카카오 도서 검색 API가 반환한 개별 도서 정보를 사용자 화면 계약으로 변환한다
     *
     * @author SeungHyeon.Kang
     */
    // 카카오 도서 검색 API에서 조회된 개별 도서 정보
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "카카오 도서 검색 결과 항목 DTO", hidden = true)
    public static class BookDto {

        // 도서 제목
        private String title;

        // 도서를 집필한 저자명
        private String author;

        // 도서를 발행한 출판사명
        private String publisher;

        // 도서를 식별하는 ISBN 값
        private String isbn;

        // 카카오 도서 검색에서 제공하는 도서 표지 이미지 URL
        @JsonAlias("thumbnail")
        private String image;

        // 도서의 주요 내용을 요약한 설명
        @JsonAlias("contents")
        private String description;

        // yyyyMMdd 형식의 도서 출간일
        private String pubdate;

        /**
         * 카카오 응답의 저자 배열을 기존 화면 계약의 구분 문자열로 변환한다
         *
         * @author SeungHyeon.Kang
         * @param authors 카카오 도서 검색 응답의 저자 목록
         */
        @JsonSetter("authors")
        public void setAuthors(List<String> authors) {
            // 저자가 없는 도서도 화면에서 안전하게 렌더링할 수 있도록 빈 문자열을 적용한다
            if (StringUtil.isEmpty(authors)) {
                this.author = StringUtil.EMPTY;

                // 저자 정보가 없는 응답의 변환을 종료한다
                return;
            }

            // 기존 화면과 저장 로직이 사용하는 저자 구분 형식으로 목록을 결합한다
            this.author = String.join("^", authors);
        }

        /**
         * 카카오 응답의 출간일시를 기존 화면 계약의 yyyyMMdd 형식으로 변환한다
         *
         * @author SeungHyeon.Kang
         * @param datetime ISO 8601 형식의 도서 출간일시
         */
        @JsonSetter("datetime")
        public void setDatetime(String datetime) {
            // 출간일이 없는 도서도 화면에서 안전하게 렌더링할 수 있도록 빈 문자열을 적용한다
            if (StringUtil.isEmpty(datetime)) {
                this.pubdate = StringUtil.EMPTY;

                // 출간일 정보가 없는 응답의 변환을 종료한다
                return;
            }

            // ISO 8601 날짜 부분만 yyyyMMdd 형식으로 변환한다
            int dateEndIndex = Math.min(datetime.length(), 10);
            // 기존 화면과 저장 로직이 사용하는 출간일 형식으로 설정한다
            this.pubdate = datetime.substring(0, dateEndIndex).replace("-", StringUtil.EMPTY);
        }

        /**
         * 카카오 응답에 ISBN10과 ISBN13이 함께 있으면 기존 데이터와 호환되는 ISBN13을 선택한다
         *
         * @author SeungHyeon.Kang
         * @param isbn 공백으로 구분된 ISBN10 또는 ISBN13 문자열
         */
        @JsonSetter("isbn")
        public void setIsbn(String isbn) {
            // ISBN이 없는 도서도 화면에서 안전하게 검증할 수 있도록 빈 문자열을 적용한다
            if (StringUtil.isEmpty(isbn)) {
                this.isbn = StringUtil.EMPTY;

                // ISBN 정보가 없는 응답의 변환을 종료한다
                return;
            }

            // ISBN10과 ISBN13을 개별 후보로 분리한다
            String[] isbnValues = isbn.trim().split("\\s+");
            // 카카오 응답에서 마지막에 제공되는 ISBN13을 우선 사용한다
            this.isbn = isbnValues[isbnValues.length - 1];
        }
    }
}
