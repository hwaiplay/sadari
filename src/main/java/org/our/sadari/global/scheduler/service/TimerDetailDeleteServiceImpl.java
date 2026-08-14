package org.our.sadari.global.scheduler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.scheduler.common.SchedulerLogSupport;
import org.our.sadari.global.scheduler.dto.SchedulerLogDto;
import org.our.sadari.timer.service.ReadingTimerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * fileName       : TimerDetailDeleteServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-08-14
 * description    : 오래된 독서 타이머 세션 상세를 삭제하고 스케줄러 로그를 기록한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        SeungHyeon.Kang    최초 생성
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TimerDetailDeleteServiceImpl implements TimerDetailDeleteService {

    // 독서 타이머 상세 삭제 서비스
    private final ReadingTimerService readingTimerService;
    // 스케줄러 실행 로그 안전 처리 객체
    private final SchedulerLogSupport schedulerLogSupport;

    /**
     * 보존기간이 지난 완료 타이머 세션 상세를 삭제하고 실행 결과를 기록한다
     *
     * @author SeungHyeon.Kang
     */
    @Override
    @Transactional
    public void delExpiredTimer() {

        long startNanoTime = System.nanoTime();
        SchedulerLogDto.SchedulerRunDto schedulerRunDto = new SchedulerLogDto.SchedulerRunDto();
        // 타이머 상세 정리 스케줄러 코드를 설정한다
        schedulerRunDto.setSchdCode(Constant.SCHEDULER_CODE_TIMER_DETAIL_DELETE);
        // 현재 실행 메서드 이름을 설정한다
        schedulerRunDto.setMethName(Thread.currentThread().getStackTrace()[1].getMethodName());
        // 실행 시작 상태를 설정한다
        schedulerRunDto.setExecStat(Constant.SCHEDULER_EXEC_RUNNING);
        // 실행 시작 일시를 설정한다
        schedulerRunDto.setStrtDate(LocalDateTime.now());
        Long runxNumb = null;
        int deletedCount = 0;
        int failureCount = 0;
        String executionStatus = Constant.SCHEDULER_EXEC_RUNNING;

        // 삭제 실패를 실행 로그와 실패 상세에 남기기 위해 예외 흐름을 분리한다
        try {
            // 보존기간이 지난 완료 세션 상세를 삭제한다
            deletedCount = readingTimerService.delExpiredTimer();
            executionStatus = deletedCount == Constant.NUMBER_ZERO
                    ? Constant.SCHEDULER_EXEC_NO_DATA : Constant.SCHEDULER_EXEC_SUCCESS;
            // 실제 삭제 건수를 운영 로그에 남긴다
            log.info("독서 타이머 상세 정리가 완료되었습니다. 삭제 건수={}", deletedCount);
        } catch (RuntimeException exception) {
            failureCount = Constant.NUMBER_ONE;
            executionStatus = Constant.SCHEDULER_EXEC_FAILURE;
            // 실패 상세를 연결할 마스터 실행 로그를 등록한다
            runxNumb = schedulerLogSupport.setSchedulerLogSafely(schedulerRunDto);
            // Java 예외를 스케줄러 실패 상세에 기록한다
            schedulerLogSupport.setSchedulerFailSafely(runxNumb, Constant.SCHEDULER_FAIL_EXCEPTION, null, null, exception);
            // 삭제 실패 원인을 운영 로그에 기록한다
            log.error("독서 타이머 상세 정리 중 오류가 발생했습니다.", exception);
            throw exception;
        } finally {
            // 실행 번호를 최종 로그에 설정한다
            schedulerRunDto.setRunxNumb(runxNumb);
            // 최종 실행 상태를 설정한다
            schedulerRunDto.setExecStat(executionStatus);
            // 삭제 대상 건수를 설정한다
            schedulerRunDto.setTrgtCntt(deletedCount);
            // 정상 삭제 건수를 설정한다
            schedulerRunDto.setSuccCntt(deletedCount);
            // 실패 건수를 설정한다
            schedulerRunDto.setFailCntt(failureCount);
            // 전체 실행 시간을 밀리초로 설정한다
            schedulerRunDto.setExecMsec(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanoTime));
            // 삭제 또는 실패가 발생한 실행만 마스터 로그에 저장한다
            if (deletedCount > Constant.NUMBER_ZERO || failureCount > Constant.NUMBER_ZERO) {
                // 예외 전에 실행 로그가 생성되지 않았다면 최종 값으로 등록한다
                if (StringUtil.isEmpty(runxNumb)) {
                    // 최종 처리 건수가 반영된 실행 로그를 등록한다
                    runxNumb = schedulerLogSupport.setSchedulerLogSafely(schedulerRunDto);
                    // 등록된 실행 번호를 종료 로그에 설정한다
                    schedulerRunDto.setRunxNumb(runxNumb);
                }
                // 스케줄러 종료 상태와 건수를 갱신한다
                schedulerLogSupport.uptSchedulerLogSafely(schedulerRunDto);
            }
        }
    }
}
