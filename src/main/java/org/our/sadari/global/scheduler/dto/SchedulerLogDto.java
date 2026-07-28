package org.our.sadari.global.scheduler.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * fileName       : SchedulerLogDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-26
 * description    : 스케줄러 실행 요약과 실패 로그 데이터를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-26        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    DTO 문서화 규칙 정비
 */
@Schema(description = "스케줄러 실행과 실패 로그 DTO", hidden = true)
public class SchedulerLogDto {

    /**
     * TL_SCLOGX의 스케줄러 실행 단위 요약 정보를 전달한다
     *
     * @author SeungHyeon.Kang
     */
    // 스케줄러 한 번의 실행 결과 요약
    @Data
    @Schema(description = "스케줄러 실행 로그 DTO", hidden = true)
    public static class SchedulerRunDto {

        // 스케줄러 실행 번호
        private Long runxNumb;

        // 스케줄러 코드
        private String schdCode;

        // 스케줄러 실행 메서드명
        private String methName;

        // 스케줄러 실행 상태
        private String execStat;

        // 스케줄러 시작 일시
        private LocalDateTime strtDate;

        // 스케줄러 종료 일시
        private LocalDateTime fnshDate;

        // 스케줄러 처리 대상 건수
        private int trgtCntt;

        // 스케줄러 성공 건수
        private int succCntt;

        // 스케줄러 실패 건수
        private int failCntt;

        // 스케줄러 실행 소요 시간
        private Long execMsec;
    }

    /**
     * TL_SCFAIL에 저장할 스케줄러 실패 한 건의 정보를 전달한다
     *
     * @author SeungHyeon.Kang
     */
    // 스케줄러 실행 중 발생한 단일 실패 정보
    @Data
    @Schema(description = "스케줄러 실패 로그 DTO", hidden = true)
    public static class SchedulerFailDto {

        // 스케줄러 실행 번호
        private Long runxNumb;

        // 실행 내 실패 순번
        private Integer failNumb;

        // 스케줄러 실패 유형
        private String failType;

        // 실패 업무 결과 코드
        private Integer rsltCode;

        // 실패 업무 결과 메시지
        private String rsltMesg;

        // 발생 예외 클래스명
        private String erroType;

        // 발생 예외 상세 내용
        private String erroCntn;

        // 스케줄러 실패 일시
        private LocalDateTime failDate;
    }
}
