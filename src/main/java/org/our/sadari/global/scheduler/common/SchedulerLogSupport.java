package org.our.sadari.global.scheduler.common;

import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.scheduler.dto.SchedulerLogDto;
import org.our.sadari.global.scheduler.service.SchedulerLogService;
import org.springframework.stereotype.Component;

/**
 * fileName       : SchedulerLogSupport
 * author         : SeungHyeon.Kang
 * date           : 2026-07-26
 * description    : 스케줄러 업무에 필요한 기능을 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-26        SeungHyeon.Kang    최초 생성
 */
@Component
@Slf4j
public class SchedulerLogSupport {
    // SchedulerLog 업무 처리 서비스
    private final SchedulerLogService schedulerLogService;

    /**
     * 별도 트랜잭션으로 로그를 저장하는 서비스를 주입받아 공통 로그 지원 객체를 생성
     *
     * @author SeungHyeon.Kang
     * @param schedulerLogService 스케줄러 실행 및 실패 로그 저장 서비스
     */
    public SchedulerLogSupport(SchedulerLogService schedulerLogService) {

        this.schedulerLogService = schedulerLogService;
    }

    /**
     * 실행 시작 로그를 등록하되 로그 저장 오류가 원래 스케줄러 업무를 중단시키지 않도록 격리
     *
     * @author SeungHyeon.Kang
     * @param schedulerRunDto 실행 시작 정보
     * @return 등록된 실행 번호, 입력값 누락 또는 로그 등록 실패 시 null
     */
    public Long setSchedulerLogSafely(SchedulerLogDto.SchedulerRunDto schedulerRunDto) {
        // 실행 정보가 없으면 로그 서비스의 필수값 검증 예외를 만들지 않고 로그 등록만 생략한다.
        if (StringUtil.isEmpty(schedulerRunDto)) {
            // 실패 원인과 처리 대상을 오류 로그로 남긴다
            log.error("스케줄러 실행 시작 로그 정보가 없어 등록을 생략했습니다.");
            // 조회하거나 생성할 값이 없음을 반환한다
            return null;
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // 실행 시작 로그를 등록하되 로그 저장 오류가 원래 스케줄러 업무를 중단시키지 않도록 격리 결과를 반환한다
            return schedulerLogService.setSchedulerLog(schedulerRunDto);
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (RuntimeException e) {
            // 실패 원인과 처리 대상을 오류 로그로 남긴다
            log.error("스케줄러 실행 시작 로그를 등록하지 못했습니다.", e);
            // 조회하거나 생성할 값이 없음을 반환한다
            return null;
        }
    }

    /**
     * 실패 상세를 등록하되 로그 저장 오류가 다음 스케줄러 대상의 처리를 막지 않도록 격리
     *
     * @author SeungHyeon.Kang
     * @param runxNumb 스케줄러 실행 번호
     * @param failType 비정상 업무 응답과 Java 예외를 구분하는 실패 유형
     * @param resultCode 업무 처리 결과 코드
     * @param resultMessage 업무 처리 결과 메시지
     * @param exception 발생한 Java 예외
     */
    public void setSchedulerFailSafely(Long runxNumb, String failType, Integer resultCode
                                     , String resultMessage, RuntimeException exception) {
        // 마스터 로그 등록에 실패했다면 연결할 실행 번호가 없으므로 고아 상세 로그의 저장을 생략한다.
        if (StringUtil.isEmpty(runxNumb)) {
            // 실패 상세를 등록하되 로그 저장 오류가 다음 스케줄러 대상의 처리를 막지 않도록 격리 결과를 반환한다
            return;
        }

        // 스케줄러 실패 상세 정보를 담을 객체를 생성한다
        SchedulerLogDto.SchedulerFailDto schedulerFailDto = new SchedulerLogDto.SchedulerFailDto();
        // RunxNumb 업무 값을 schedulerFailDto DTO에 설정한다
        schedulerFailDto.setRunxNumb(runxNumb);
        // FailType 업무 값을 schedulerFailDto DTO에 설정한다
        schedulerFailDto.setFailType(failType);
        // RsltCode 업무 값을 schedulerFailDto DTO에 설정한다
        schedulerFailDto.setRsltCode(resultCode);
        // RsltMesg 업무 값을 schedulerFailDto DTO에 설정한다
        schedulerFailDto.setRsltMesg(resultMessage);

        // 비정상 ResultData 응답과 달리 Java 예외에는 예외 클래스와 메시지를 오류 전용 컬럼에 보관한다.
        if (!StringUtil.isEmpty(exception)) {
            // ErroType 업무 값을 schedulerFailDto DTO에 설정한다
            schedulerFailDto.setErroType(exception.getClass().getName());
            // ErroCntn 업무 값을 schedulerFailDto DTO에 설정한다
            schedulerFailDto.setErroCntn(exception.getMessage());
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // SchedulerFail 업무 값을 schedulerLogService DTO에 설정한다
            schedulerLogService.setSchedulerFail(schedulerFailDto);
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (RuntimeException e) {
            // 실패 원인과 처리 대상을 오류 로그로 남긴다
            log.error("스케줄러 실패 상세 로그를 등록하지 못했습니다. 실행 번호={}", runxNumb, e);
        }
    }

    /**
     * 실행 종료 로그를 수정하되 로그 수정 오류가 스케줄러의 원래 처리 결과를 덮어쓰지 않도록 격리
     *
     * @author SeungHyeon.Kang
     * @param schedulerRunDto 실행 종료 정보
     */
    public void uptSchedulerLogSafely(SchedulerLogDto.SchedulerRunDto schedulerRunDto) {
        // 시작 로그가 등록되지 않았다면 수정할 마스터 행이 없으므로 종료 상태 갱신만 생략한다.
        if (StringUtil.isEmpty(schedulerRunDto) || StringUtil.isEmpty(schedulerRunDto.getRunxNumb())) {
            // 실행 종료 로그를 수정하되 로그 수정 오류가 스케줄러의 원래 처리 결과를 덮어쓰지 않도록 격리 결과를 반환한다
            return;
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // uptSchedulerLog 업무 로직을 schedulerLogService에 위임한다
            schedulerLogService.uptSchedulerLog(schedulerRunDto);
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (RuntimeException e) {
            // 실패 원인과 처리 대상을 오류 로그로 남긴다
            log.error("스케줄러 실행 종료 로그를 수정하지 못했습니다. 실행 번호={}", schedulerRunDto.getRunxNumb(), e);
        }
    }

    /**
     * 성공 및 실패 건수를 기준으로 스케줄러 마스터 로그에 저장할 최종 실행 상태를 결정
     *
     * @author SeungHyeon.Kang
     * @param successCnt 성공 건수
     * @param failureCnt 실패 건수
     * @return 성공, 일부 실패, 실패 중 하나의 실행 상태
     */
    public String getSchedulerExecutionStatus(int successCnt, int failureCnt) {
        // 실패가 한 건도 없으면 조회된 모든 대상이 성공한 상태이다.
        if (failureCnt == 0) {
            // 성공 및 실패 건수를 기준으로 스케줄러 마스터 로그에 저장할 최종 실행 상태를 결정 결과를 반환한다
            return Constant.SCHEDULER_EXEC_SUCCESS;
        }

        // 성공과 실패가 함께 있으면 관리자가 일부 대상만 재확인할 수 있도록 일부 실패로 구분한다.
        if (successCnt > 0) {
            // 성공 및 실패 건수를 기준으로 스케줄러 마스터 로그에 저장할 최종 실행 상태를 결정 결과를 반환한다
            return Constant.SCHEDULER_EXEC_PARTIAL;
        }

        // 성공 및 실패 건수를 기준으로 스케줄러 마스터 로그에 저장할 최종 실행 상태를 결정 결과를 반환한다
        return Constant.SCHEDULER_EXEC_FAILURE;
    }
}
