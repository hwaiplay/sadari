package org.our.sadari.global.scheduler.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.scheduler.common.SchedulerLogSupport;
import org.our.sadari.global.scheduler.dto.SchedulerLogDto;
import org.our.sadari.global.scheduler.mapper.AlimDeleteMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자가 삭제한 알림의 물리 삭제와 스케줄러 실행 로그 기록을 처리
 * 읽음 여부와 삭제 여부는 별도 정책이므로 DELT_YSNO가 Y인 데이터만 삭제 대상으로 삼습니다.
 *
 * @author Seunghyeon.Kang
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlimDeleteServiceImpl implements AlimDeleteService {

    private final AlimDeleteMapper alimDeleteMapper;
    private final SchedulerLogSupport schedulerLogSupport;

    /**
     * 삭제 상태 알림을 단일 DELETE 문으로 정리하고 삭제 건수를 TL_SCLOGX에 기록
     * SQL 실행 자체가 실패하면 TL_SCFAIL에 예외 정보를 남기고 원래 예외를 다시 전달
     *
     * @author Seunghyeon.Kang
     */
    @Override
    @Transactional
    public void delAlim() {
        long startNanoTime = System.nanoTime();
        SchedulerLogDto.SchedulerRunDto schedulerRunDto = new SchedulerLogDto.SchedulerRunDto();
        schedulerRunDto.setSchdCode(Constant.SCHEDULER_CODE_ALIM_DELETE);
        schedulerRunDto.setMethName(Thread.currentThread().getStackTrace()[1].getMethodName());
        schedulerRunDto.setExecStat(Constant.SCHEDULER_EXEC_RUNNING);

        Long runxNumb = schedulerLogSupport.setSchedulerLogSafely(schedulerRunDto);
        int targetCnt = 0;
        int successCnt = 0;
        int failureCnt = 0;
        String executionStatus = Constant.SCHEDULER_EXEC_RUNNING;

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

            log.info("삭제 상태 알림 정리가 완료되었습니다. 삭제 건수={}", deletedCnt);
        } catch (RuntimeException e) {
            // 단일 DELETE가 실패하면 이번 실행 전체가 실패한 것이므로 마스터와 실패 상세에 각각 결과를 남긴다.
            failureCnt = 1;
            executionStatus = Constant.SCHEDULER_EXEC_FAILURE;
            schedulerLogSupport.setSchedulerFailSafely(
                    runxNumb
                  , Constant.SCHEDULER_FAIL_EXCEPTION
                  , null
                  , null
                  , e
            );
            log.error("삭제 상태 알림 정리 중 오류가 발생했습니다.", e);
            throw e;
        } finally {
            schedulerRunDto.setRunxNumb(runxNumb);
            schedulerRunDto.setExecStat(executionStatus);
            schedulerRunDto.setTrgtCntt(targetCnt);
            schedulerRunDto.setSuccCntt(successCnt);
            schedulerRunDto.setFailCntt(failureCnt);
            schedulerRunDto.setExecMsec(
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanoTime)
            );
            schedulerLogSupport.uptSchedulerLogSafely(schedulerRunDto);
        }
    }
}
