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
 * description    : 삭제 상태 알림을 물리 삭제하고 스케줄러 실행 결과를 기록함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    삭제 대상이 없는 실행의 로그 저장 방지
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
     * 삭제 상태 알림을 물리 삭제하고 실제 삭제 건수가 있는 실행 결과만 기록함
     * 삭제 SQL이 실패하면 실행 로그와 실패 상세를 기록한 후 원래 예외를 다시 전달함
     *
     * @author SeungHyeon.Kang
     */
    @Override
    @Transactional
    public void delAlim() {
        // 실행 시간을 측정할 시작 시각을 기록함
        long startNanoTime = System.nanoTime();
        // 스케줄러 실행 로그를 담을 객체를 생성함
        SchedulerLogDto.SchedulerRunDto schedulerRunDto = new SchedulerLogDto.SchedulerRunDto();
        // 알림 삭제 스케줄러를 식별하는 상세코드를 실행 로그에 설정함
        schedulerRunDto.setSchdCode(Constant.SCHEDULER_CODE_ALIM_DELETE);
        // 현재 실행 중인 알림 삭제 메서드명을 실행 로그에 설정함
        schedulerRunDto.setMethName(Thread.currentThread().getStackTrace()[1].getMethodName());
        // 삭제 처리가 끝나기 전까지 실행 상태를 진행 중으로 설정함
        schedulerRunDto.setExecStat(Constant.SCHEDULER_EXEC_RUNNING);
        // 알림 삭제를 시작한 시각을 실행 로그에 설정함
        schedulerRunDto.setStrtDate(LocalDateTime.now());
        Long runxNumb = null;
        int targetCnt = 0;
        int successCnt = 0;
        int failureCnt = 0;
        String executionStatus = Constant.SCHEDULER_EXEC_RUNNING;

        // 알림 삭제 SQL 실패를 스케줄러 실패 로그로 전환하기 위해 예외 흐름을 분리함
        try {
            /*
             * 삭제 대상 조회와 삭제를 분리하면 두 SQL 사이에 데이터가 추가될 수 있어 로그 건수와 실제 삭제 건수가 달라짐
             * DELETE 반환 건수를 대상 건수와 성공 건수로 함께 사용하여 실제 반영 결과를 정확하게 기록함
             */
            int deletedCnt = alimDeleteMapper.delAlim();
            targetCnt = deletedCnt;
            successCnt = deletedCnt;
            // 실제 삭제 건수에 따라 대상 없음과 정상 완료 상태를 구분함
            executionStatus = deletedCnt == 0
                    ? Constant.SCHEDULER_EXEC_NO_DATA
                    : Constant.SCHEDULER_EXEC_SUCCESS;

            // 실제 삭제된 알림 건수를 운영 로그에 기록함
            log.info("삭제 상태 알림 정리가 완료되었습니다. 삭제 건수={}", deletedCnt);
        }

        // 삭제 SQL 예외를 실행 실패와 실패 상세 로그에 함께 기록함
        catch (RuntimeException e) {
            // 단일 DELETE 실패는 이번 스케줄러 실행 전체의 실패로 집계함
            failureCnt = 1;
            executionStatus = Constant.SCHEDULER_EXEC_FAILURE;
            // 실패 마스터 로그가 생성될 수 있도록 실패 건수를 먼저 설정함
            schedulerRunDto.setFailCntt(failureCnt);
            // 실패 상세를 연결할 실행 번호가 없으면 예외 시점에 마스터 로그를 생성함
            if (StringUtil.isEmpty(runxNumb)) {
                // 실패 상세가 참조할 스케줄러 마스터 로그를 안전하게 등록함
                runxNumb = schedulerLogSupport.setSchedulerLogSafely(schedulerRunDto);
            }

            // 알림 삭제 중 발생한 예외 유형과 내용을 실패 상세 로그에 기록함
            schedulerLogSupport.setSchedulerFailSafely(
                    runxNumb
                  , Constant.SCHEDULER_FAIL_EXCEPTION
                  , null
                  , null
                  , e
            );
            // 알림 삭제 실패 원인과 예외 스택을 운영 로그에 기록함
            log.error("삭제 상태 알림 정리 중 오류가 발생했습니다.", e);
            throw e;
        }

        // 정상 완료와 예외 발생 모두에서 최종 실행 건수와 소요 시간을 확정함
        finally {
            // 기존에 생성된 스케줄러 실행 번호를 최종 실행 정보에 설정함
            schedulerRunDto.setRunxNumb(runxNumb);
            // 삭제 결과로 결정된 최종 실행 상태를 설정함
            schedulerRunDto.setExecStat(executionStatus);
            // 실제 DELETE 반영 건수를 처리 대상 건수로 설정함
            schedulerRunDto.setTrgtCntt(targetCnt);
            // 정상적으로 삭제된 알림 건수를 성공 건수로 설정함
            schedulerRunDto.setSuccCntt(successCnt);
            // 삭제 SQL 실행 중 발생한 실패 건수를 설정함
            schedulerRunDto.setFailCntt(failureCnt);
            // 알림 삭제 스케줄러의 전체 실행 시간을 밀리초 단위로 설정함
            schedulerRunDto.setExecMsec(
                    // 나노초 단위 실행 시간을 스케줄러 로그 저장 단위인 밀리초로 변환함
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanoTime)
            );
            // 삭제 대상이나 실패가 있는 실행만 관리자 확인용 스케줄러 로그로 관리함
            if (targetCnt > 0 || successCnt > 0 || failureCnt > 0) {
                // 처리 건수가 있지만 마스터 로그가 없으면 최종 건수로 실행 로그를 생성함
                if (StringUtil.isEmpty(runxNumb)) {
                    // 최종 처리 건수가 반영된 스케줄러 마스터 로그를 등록함
                    runxNumb = schedulerLogSupport.setSchedulerLogSafely(schedulerRunDto);
                    // 등록된 실행 번호를 종료 로그 수정에 사용하도록 설정함
                    schedulerRunDto.setRunxNumb(runxNumb);
                }

                // 등록된 스케줄러 마스터 로그에 최종 실행 상태와 처리 건수를 반영함
                schedulerLogSupport.uptSchedulerLogSafely(schedulerRunDto);
            }
        }
    }
}
