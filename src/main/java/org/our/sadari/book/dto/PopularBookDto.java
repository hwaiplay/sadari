package org.our.sadari.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * fileName       : PopularBookDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-16
 * description    : 선택 기간의 독후감 작성자 수 기준 인기 도서 정보를 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-16        SeungHyeon.Kang    최초 생성 및 평균 평점 추가
 */
@Data
@NoArgsConstructor
@Schema(description = "기간별 인기 도서 응답 DTO", hidden = true)
public class PopularBookDto {

    // 선택 기간의 인기 도서 순위
    private Integer rank;

    // 선택 기간에 해당 도서를 기록한 고유 독후감 작성자 수
    private Long reportCount;

    // 읽는 중을 제외한 해당 도서의 전체 독후감 평균 평점
    private BigDecimal ratingAverage;

    // 도서 제목
    private String title;

    // 도서를 집필한 저자명
    private String author;

    // 도서를 발행한 출판사명
    private String publisher;

    // 도서를 식별하는 ISBN 값
    private String isbn;

    // 저장된 도서 표지 이미지 URL
    private String image;

    // 저장된 도서 소개
    private String description;

    // yyyyMMdd 형식의 도서 출간일
    private String pubdate;
}
