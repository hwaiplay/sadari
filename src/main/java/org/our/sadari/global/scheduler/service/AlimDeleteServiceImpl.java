package org.our.sadari.global.scheduler.service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.scheduler.common.SchedulerLogSupport;
import org.our.sadari.global.scheduler.dto.SchedulerLogDto;
import org.our.sadari.global.scheduler.mapper.AlimDeleteMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : AlimDeleteServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 스케줄러 업무 로직을 구현한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AlimDeleteServiceImpl implements AlimDeleteService {

    // AlimDelete 데이터 접근 객체
    private final AlimDeleteMapper alimDeleteMapper;
    // 스케줄러 로그 안전 처리 객체
    private final SchedulerLogSupport schedulerLogSupport;

    /**
     * 삭제 상태 알림을 단일 DELETE 문으로 정리하고 삭제 건수를 TL_SCLOGX에 기록
     * SQL 실행 자체가 실패하면 TL_SCFAIL에 예외 정보를 남기고 원래 예외를 다시 전달
     *
     * @author SeungHyeon.Kang
     */
    @Override
    @Transactional
    public void delAlim() {
        // 실행 시간을 측정할 시작 시각을 기록한다
        long startNanoTime = System.nanoTime();
        // 스케줄러 실행 로그를 담을 객체를 생성한다
        SchedulerLogDto.SchedulerRunDto schedulerRunDto = new SchedulerLogDto.SchedulerRunDto();
        // SchdCode 업무 값을 schedulerRunDto DTO에 설정한다
        schedulerRunDto.setSchdCode(Constant.SCHEDULER_CODE_ALIM_DELETE);
        // MethName 업무 값을 schedulerRunDto DTO에 설정한다
        schedulerRunDto.setMethName(Thread.currentThread().getStackTrace()[1].getMethodName());
        // ExecStat 업무 값을 schedulerRunDto DTO에 설정한다
        schedulerRunDto.setExecStat(Constant.SCHEDULER_EXEC_RUNNING);
        // StrtDate 업무 값을 schedulerRunDto DTO에 설정한다
        schedulerRunDto.setStrtDate(LocalDateTime.now());
        Long runxNumb = null;
        int targetCnt = 0;
        int successCnt = 0;
        int failureCnt = 0;
        String executionStatus = Constant.SCHEDULER_EXEC_RUNNING;

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            /*
             * 삭제 대상 조회와 삭제를 분리하면 두 SQL 사이에 데이터가 추가될 수 있어 로그 건수와 실제 삭제 건수가 달라진다.
             * DELETE 반환 건수를 대상 건수와 성공 건수로 함께 사용하여 실제 반영 결과를 정확하게 기록한다.
             */
            int deletedCnt = alimDeleteMapper.delAlim();
            targetCnt = deletedCnt;
            successCnt = deletedCnt;
            executionStatus = deletedCnt == 0
                    ? Constant.SCHEDULER_EXEC_NO_DATA
                    : Constant.SCHEDULER_EXEC_SUCCESS;

            // 처리 상태를 정보 로그로 남긴다
            log.info("삭제 상태 알림 정리가 완료되었습니다. 삭제 건수={}", deletedCnt);
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (RuntimeException e) {
            // 단일 DELETE가 실패하면 이번 실행 전체가 실패한 것이므로 마스터와 실패 상세에 각각 결과를 남긴다.
            failureCnt = 1;
            executionStatus = Constant.SCHEDULER_EXEC_FAILURE;
            // FailCntt 업무 값을 schedulerRunDto DTO에 설정한다
            schedulerRunDto.setFailCntt(failureCnt);
            // 실패 상세를 연결할 실행 번호가 없으면 최초 예외 시점에 마스터 로그를 생성한다.
            if (StringUtil.isEmpty(runxNumb)) {
                // SchedulerLogSafely 업무 값을 schedulerLogSupport DTO에 설정한다
                runxNumb = schedulerLogSupport.setSchedulerLogSafely(schedulerRunDto);
            }

            // SchedulerFailSafely 업무 값을 schedulerLogSupport DTO에 설정한다
            schedulerLogSupport.setSchedulerFailSafely(
                    runxNumb
                  , Constant.SCHEDULER_FAIL_EXCEPTION
                  , null
                  , null
                  , e
            );
            // 실패 원인과 처리 대상을 오류 로그로 남긴다
            log.error("삭제 상태 알림 정리 중 오류가 발생했습니다.", e);
            throw e;
        }

        // 성공 여부와 관계없이 반드시 자원을 정리하기 위한 블록이다
        finally {
            // RunxNumb 업무 값을 schedulerRunDto DTO에 설정한다
            schedulerRunDto.setRunxNumb(runxNumb);
            // ExecStat 업무 값을 schedulerRunDto DTO에 설정한다
            schedulerRunDto.setExecStat(executionStatus);
            // TrgtCntt 업무 값을 schedulerRunDto DTO에 설정한다
            schedulerRunDto.setTrgtCntt(targetCnt);
            // SuccCntt 업무 값을 schedulerRunDto DTO에 설정한다
            schedulerRunDto.setSuccCntt(successCnt);
            // FailCntt 업무 값을 schedulerRunDto DTO에 설정한다
            schedulerRunDto.setFailCntt(failureCnt);
            // ExecMsec 업무 값을 schedulerRunDto DTO에 설정한다
            schedulerRunDto.setExecMsec(
                    // 측정한 실행 시간을 밀리초 단위로 변환한다
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanoTime)
            );
            // 삭제 또는 실패 건수가 존재하지만 마스터 로그를 만들지 않았다면 최종 건수로 로그를 생성한다.
            if (StringUtil.isEmpty(runxNumb)) {

                // SchedulerLogSafely 업무 값을 schedulerLogSupport DTO에 설정한다
                runxNumb = schedulerLogSupport.setSchedulerLogSafely(schedulerRunDto);
                // RunxNumb 업무 값을 schedulerRunDto DTO에 설정한다
                schedulerRunDto.setRunxNumb(runxNumb);
            }

            // uptSchedulerLogSafely 호출로 변경된 업무 상태를 반영한다
            schedulerLogSupport.uptSchedulerLogSafely(schedulerRunDto);
        }
    }
}
