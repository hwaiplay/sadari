package org.our.sadari.global.scheduler.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 스케줄러 실행 요약 로그와 실패 상세 로그에서 사용하는 DTO를 용도별 중첩 클래스로 정의
 *
 * @author Seunghyeon.Kang
 */
public class SchedulerLogDto {

    /**
     * TL_SCLOGX의 스케줄러 실행 단위 요약 정보를 전달
     *
     * @author Seunghyeon.Kang
     */
    @Data
    public static class SchedulerRunDto {

        /** 스케줄러 실행 번호 */
        private Long runxNumb;

        /** 실행한 스케줄러를 식별하는 코드 */
        private String schdCode;

        /** 현재 스레드에서 확인한 실행 메서드명 */
        private String methName;

        /** 실행 중, 성공, 일부 실패, 실패, 대상 없음 중 하나의 실행 상태 */
        private String execStat;

        /** 스케줄러 실행 시작 일시 */
        private LocalDateTime strtDate;

        /** 스케줄러 실행 종료 일시 */
        private LocalDateTime fnshDate;

        /** 이번 실행에서 조회한 전체 대상 건수 */
        private int trgtCntt;

        /** 정상적으로 처리한 대상 건수 */
        private int succCntt;

        /** 비정상 응답 또는 예외가 발생한 대상 건수 */
        private int failCntt;

        /** 스케줄러 전체 실행 소요 시간의 밀리초 값 */
        private Long execMsec;
    }

    /**
     * TL_SCFAIL에 저장할 스케줄러 실패 한 건의 정보를 전달
     *
     * @author Seunghyeon.Kang
     */
    @Data
    public static class SchedulerFailDto {

        /** 실패가 발생한 스케줄러 실행 번호 */
        private Long runxNumb;

        /** 동일 실행 번호 안에서 MAX+1로 계산되는 실패 순번 */
        private Integer failNumb;

        /** 비정상 업무 응답과 Java 예외를 구분하는 실패 유형 */
        private String failType;

        /** ResultData가 반환한 업무 처리 결과 코드 */
        private Integer rsltCode;

        /** ResultData가 반환한 업무 처리 결과 메시지 */
        private String rsltMesg;

        /** 발생한 Java 예외의 전체 클래스명 */
        private String erroType;

        /** 발생한 예외 또는 내부 오류의 상세 내용 */
        private String erroCntn;

        /** 실패가 기록된 일시 */
        private LocalDateTime failDate;
    }
}
