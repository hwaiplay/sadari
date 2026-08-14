package org.our.sadari.myPage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * fileName       : ReadingStatisticsDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-14
 * description    : 마이페이지의 독서 시간과 습관 및 독후감 통계를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        SeungHyeon.Kang    최초 생성
 * 2026-08-14        SeungHyeon.Kang    연속 독서와 책별 시간 및 별점과 연도 비교 통계 추가
 * 2026-08-14        SeungHyeon.Kang    별점 분포를 소수점 버림한 1점 단위로 변경
 * 2026-08-14        SeungHyeon.Kang    올해 상위 도서의 독후감 이동 번호 추가
 */
@Data
@Schema(description = "본인 및 공개 프로필 독서 통계 DTO")
public class ReadingStatisticsDto {

    @Schema(description = "독서 시간 잔디 시작일", example = "2025-08-15")
    private LocalDate heatmapStart;
    @Schema(description = "독서 시간 잔디 종료일", example = "2026-08-14")
    private LocalDate heatmapEnd;
    @Schema(description = "선택 연도의 일별 독서 시간 목록")
    private List<Daily> heatmapList;
    @Schema(description = "읽는 중, 완독, 중단 상태별 독후감 수 목록")
    private List<Status> statusList;
    @Schema(description = "현재 및 최장 연속 독서일")
    private Streak streak;
    @Schema(description = "현재 연도 타이머 독서 시간이 긴 도서 상위 목록")
    private List<BookTime> topBookList;
    @Schema(description = "소수점 별점을 버림한 0점부터 5점까지의 별점별 독후감 수 목록")
    private List<Rating> ratingList;
    @Schema(description = "현재 연도와 전년도 같은 기간의 독서 기록 비교")
    private YearComparison yearComparison;
    @Schema(description = "잔디에 표시한 연도", example = "2026")
    private int selectedYear;
    @Schema(description = "잔디로 조회할 수 있는 연도 목록", example = "[2026, 2025]")
    private List<Integer> availableYears;
    @Schema(description = "다른 사용자에게 독서 통계를 공개할지 여부", example = "N", allowableValues = {"Y", "N"})
    private String publicYsno;

    /**
     * 잔디 한 칸에 표시할 날짜별 독서 시간을 전달한다
     *
     * @author SeungHyeon.Kang
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "날짜별 독서 시간 DTO")
    public static class Daily {

        @Schema(description = "독서 날짜", example = "2026-08-14")
        private LocalDate readDate;
        @Schema(description = "타이머로 확정한 독서 시간 초", example = "1800")
        private long readSecs;
    }

    /**
     * 독서 상태 비율에 사용할 상태별 독후감 수를 전달한다
     *
     * @author SeungHyeon.Kang
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "독서 상태별 독후감 수 DTO")
    public static class Status {

        @Schema(description = "독서 상태 코드", example = "DONE", allowableValues = {"READ", "DONE", "STOP"})
        private String reptStat;
        @Schema(description = "해당 상태의 독후감 수", example = "12")
        private long reptCnt;
    }

    /**
     * 타이머 기록이 이어진 현재 및 최장 연속 독서일을 전달한다
     *
     * @author SeungHyeon.Kang
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "연속 독서 기록 DTO")
    public static class Streak {

        @Schema(description = "오늘 또는 어제까지 이어진 현재 연속 독서일", example = "7")
        private int currentStreakDays;
        @Schema(description = "전체 타이머 기록 중 최장 연속 독서일", example = "21")
        private int longestStreakDays;
    }

    /**
     * 현재 연도에 타이머로 오래 읽은 도서와 누적 시간을 전달한다
     *
     * @author SeungHyeon.Kang
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "도서별 독서 시간 DTO")
    public static class BookTime {

        @Schema(description = "본인 마이페이지에서 이동할 독후감 번호", example = "31")
        private Long reptNumb;
        @Schema(description = "도서 번호", example = "15")
        private Long bookNumb;
        @Schema(description = "도서 제목", example = "사다리 독서법")
        private String bookTitl;
        @Schema(description = "도서 저자", example = "홍길동")
        private String bookAthr;
        @Schema(description = "도서 표지 이미지 URL")
        private String bookCvim;
        @Schema(description = "현재 연도에 타이머로 확정한 독서 시간 초", example = "14400")
        private long readSecs;
    }

    /**
     * 유효한 별점별 독후감 수를 전달한다
     *
     * @author SeungHyeon.Kang
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "별점 분포 DTO")
    public static class Rating {

        @Schema(description = "원본 별점의 소수점을 버림한 1점 단위 별점", example = "4")
        private BigDecimal reptGrde;
        @Schema(description = "해당 별점의 독후감 수", example = "8")
        private long reptCnt;
    }

    /**
     * 현재 연도와 전년도 같은 기간의 독서 시간과 독서일 및 완독 권수를 전달한다
     *
     * @author SeungHyeon.Kang
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "연도별 독서 기록 비교 DTO")
    public static class YearComparison {

        @Schema(description = "현재 연도", example = "2026")
        private int currentYear;
        @Schema(description = "비교할 이전 연도", example = "2025")
        private int previousYear;
        @Schema(description = "현재 연도 같은 기간의 확정 독서 시간 초", example = "36000")
        private long currentReadSecs;
        @Schema(description = "이전 연도 같은 기간의 확정 독서 시간 초", example = "28800")
        private long previousReadSecs;
        @Schema(description = "현재 연도 같은 기간의 독서일", example = "18")
        private long currentReadDays;
        @Schema(description = "이전 연도 같은 기간의 독서일", example = "14")
        private long previousReadDays;
        @Schema(description = "현재 연도 같은 기간의 완독 권수", example = "9")
        private long currentDoneBooks;
        @Schema(description = "이전 연도 같은 기간의 완독 권수", example = "6")
        private long previousDoneBooks;
    }
}
