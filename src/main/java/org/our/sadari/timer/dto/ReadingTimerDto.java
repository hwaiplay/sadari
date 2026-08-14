package org.our.sadari.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * fileName       : ReadingTimerDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-14
 * description    : 독서 타이머 세션과 주간 출석 데이터를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        SeungHyeon.Kang    최초 생성
 * 2026-08-14        SeungHyeon.Kang    오늘 완료 타이머 응답 설명 반영
 */
@Data
public class ReadingTimerDto {

    @Schema(description = "독서 타이머 세션 번호")
    private Long tmrxNumb;
    @Schema(hidden = true)
    private Long userNumb;
    @Schema(description = "연결 독후감 번호")
    private Long reptNumb;
    @Schema(description = "연결 도서 제목")
    private String bookTitl;
    @Schema(description = "연결 도서 표지 URL")
    private String bookCvim;
    @Schema(description = "타이머 상태")
    private String tmrxStat;
    @Schema(description = "세션 시작 일시")
    private LocalDateTime strtDate;
    @Schema(description = "최근 측정 시작 일시")
    private LocalDateTime lastStrt;
    @Schema(description = "세션 완료 일시")
    private LocalDateTime endxDate;
    @Schema(description = "확정 독서 시간 초")
    private long readSecs;
    @Schema(description = "등록 일시")
    private LocalDateTime regiDate;
    @Schema(description = "수정 일시")
    private LocalDateTime updtDate;

    /**
     * 타이머 시작 또는 상태 변경 요청을 전달한다
     *
     * @author SeungHyeon.Kang
     */
    @Data
    public static class Request {
        @Schema(description = "연결할 독후감 번호")
        private Long reptNumb;
        @Schema(description = "변경할 타이머 상태", allowableValues = {"RUNNING", "PAUSED", "COMPLETED"})
        private String tmrxStat;
    }

    /**
     * 주간 일자별 독서 시간과 출석 여부를 전달한다
     *
     * @author SeungHyeon.Kang
     */
    @Data
    public static class Daily {
        @Schema(description = "독서 날짜")
        private LocalDate readDate;
        @Schema(description = "해당 날짜 독서 시간 초")
        private long readSecs;
        @Schema(description = "출석 달성 여부")
        private boolean attended;
        @Schema(description = "오늘 여부")
        private boolean today;
    }

    /**
     * 타이머 화면에 필요한 요약 데이터를 전달한다
     *
     * @author SeungHyeon.Kang
     */
    @Data
    public static class Summary {
        @Schema(description = "현재 진행 또는 일시정지 세션")
        private ReadingTimerDto activeTimer;
        @Schema(description = "이번 주 월요일")
        private LocalDate weekStart;
        @Schema(description = "이번 주 일요일")
        private LocalDate weekEnd;
        @Schema(description = "서버 현재 일시")
        private LocalDateTime serverDate;
        @Schema(description = "오늘 독서 시간 초")
        private long todayReadSecs;
        @Schema(description = "출석 인정 최소 시간 초")
        private long attendanceMinSecs;
        @Schema(description = "단일 세션 최대 시간 초")
        private long maxSessionSecs;
        @Schema(description = "이번 주 출석 일수")
        private int weekAttendanceCount;
        @Schema(description = "이번 주 일별 출석 목록")
        private java.util.List<Daily> weekList;
        @Schema(description = "타이머에 연결할 수 있는 읽는 중 도서 목록")
        private java.util.List<ReadingTimerDto> currentReadingList;
        @Schema(description = "오늘 완료한 타이머 목록")
        private java.util.List<ReadingTimerDto> recentSessionList;
    }
}
