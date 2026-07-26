package org.our.sadari.global.scheduler.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.alim.service.AlimService;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.scheduler.dto.SchedulerLogDto;
import org.our.sadari.global.scheduler.mapper.ReportDateOverMapper;
import org.our.sadari.report.dto.ReportDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 스케줄러 대상 조회와 목표 독서기간 초과 알림 발송 업무를 처리.
 *
 * @author Seunghyeon.Kang
 */
@Service
@Slf4j
public class ReportDateOverServiceImpl implements ReportDateOverService {

    private static final int RESULT_SUCCESS_CODE = 200;
    private static final String EMPTY_RESULT_MESSAGE = "알림 서비스 응답이 없습니다.";

    private final ReportDateOverMapper reportDateOverMapper;
    private final AlimService alimService;
    private final SchedulerLogService schedulerLogService;
    private final int maxSize;

    /**
     * 환경별 yml의 scheduler.max-size를 주입받아 한 번에 조회할 대상 수를 결정
     * 별도 설정 객체를 만들지 않고 단일 숫자 설정을 생성자에서 직접 주입해 테스트에서도 같은 제한값을 명시할 수 있음
     *
     * @author Seunghyeon.Kang
     * @param reportDateOverMapper 목표기간 초과 대상을 제한 조회하는 Mapper
     * @param alimService 알림 저장과 FCM 푸시 발송을 담당하는 서비스
     * @param schedulerLogService 스케줄러 실행 결과와 실패 상세를 기록하는 서비스
     * @param maxSize 한 번의 실행에서 조회할 최대 대상 수
     */
    public ReportDateOverServiceImpl(
            ReportDateOverMapper reportDateOverMapper
          , AlimService alimService
          , SchedulerLogService schedulerLogService
          , @Value("${scheduler.max-size}") int maxSize) {

        this.reportDateOverMapper = reportDateOverMapper;
        this.alimService = alimService;
        this.schedulerLogService = schedulerLogService;
        this.maxSize = maxSize;
    }

    /**
     * yml에 설정한 최대 건수만큼 대상을 조회하고 각 대상에게 목표기간 초과 알림을 발송
     * 한 대상의 발송 실패가 나머지 대상을 중단시키지 않도록 대상 단위로 예외를 격리
     *
     * @author Seunghyeon.Kang
     */
    @Override
    public void sendReportDateOverAlim() {
        long startNanoTime = System.nanoTime();
        SchedulerLogDto.SchedulerRunDto schedulerRunDto = new SchedulerLogDto.SchedulerRunDto();
        schedulerRunDto.setSchdCode(Constant.SCHEDULER_CODE_REPORT_DATE_OVER);

        /*
         * 공통 유틸리티에서 stack index를 계산하면 호출 깊이에 따라 다른 메서드명이 저장될 수 있다.
         * 실제 스케줄러 업무 메서드 안에서 현재 Thread의 두 번째 stack frame을 읽어 sendReportDateOverAlim을 기록한다.
         */
        schedulerRunDto.setMethName(Thread.currentThread().getStackTrace()[1].getMethodName());
        schedulerRunDto.setExecStat(Constant.SCHEDULER_EXEC_RUNNING);

        Long runxNumb = setSchedulerLogSafely(schedulerRunDto);
        int targetCnt = 0;
        int successCnt = 0;
        int failureCnt = 0;
        String executionStatus = Constant.SCHEDULER_EXEC_RUNNING;

        try {
            List<ReportDto> targetList = reportDateOverMapper.getReportDateOverTargetList(maxSize);

            // MyBatis는 일반적으로 빈 List를 반환하지만 비정상 null 반환도 대상 없음으로 보정해 NPE를 차단한다.
            if (targetList == null || targetList.isEmpty()) {
                executionStatus = Constant.SCHEDULER_EXEC_NO_DATA;
                log.debug("목표 독서기간 초과 알림 대상이 없습니다. 최대 조회 건수={}", maxSize);
                return;
            }

            targetCnt = targetList.size();

            for (ReportDto target : targetList) {
                try {
                    /*
                     * Mapper가 ReportDto에 독후감 번호, 사용자 번호, 책 제목을 함께 담아 반환한다.
                     * 조회한 제목을 그대로 치환 Map에 사용하므로 알림 발송 과정에서 도서 정보를 다시 조회하지 않는다.
                     */
                    ResultData result = alimService.sendAlim(
                            target.getUserNumb()
                          , Constant.ALIM_SITU_REPORT
                          , Constant.ALIM_TEMP_CODE_REPORT_DATE_OVER
                          , target.getReptNumb()
                          , Map.of("bookTitl", target.getBookTitl())
                    );

                    // ResultData가 존재하고 성공 코드인 경우에만 성공 건수로 집계한다.
                    if (result != null && result.getCode() == RESULT_SUCCESS_CODE) {
                        successCnt++;
                        continue;
                    }

                    failureCnt++;
                    Integer resultCode = result == null ? null : result.getCode();
                    String resultMessage = result == null ? EMPTY_RESULT_MESSAGE : result.getMessage();
                    setSchedulerFailSafely(
                            runxNumb
                          , Constant.SCHEDULER_FAIL_REJECTED
                          , resultCode
                          , resultMessage
                          , null
                    );

                    log.warn(
                            "목표 독서기간 초과 알림 발송이 거부되었습니다. 사용자 번호={}, 독후감 번호={}, 응답 코드={}"
                          , target.getUserNumb()
                      , target.getReptNumb()
                      , resultCode
                    );
                } catch (RuntimeException e) {
                    /*
                     * 한 대상의 예외가 남은 배치를 중단시키지 않도록 실패 건만 기록하고 다음 대상으로 진행한다.
                     * 저장되지 않은 대상은 조회 SQL의 NOT EXISTS 조건을 계속 만족하므로 다음 실행에서 다시 시도된다.
                     */
                    failureCnt++;
                    setSchedulerFailSafely(
                            runxNumb
                          , Constant.SCHEDULER_FAIL_EXCEPTION
                          , null
                          , null
                          , e
                    );
                    log.error(
                            "목표 독서기간 초과 알림 발송 중 오류가 발생했습니다. 사용자 번호={}, 독후감 번호={}"
                          , target.getUserNumb()
                          , target.getReptNumb()
                          , e
                    );
                }
            }

            executionStatus = getSchedulerExecutionStatus(successCnt, failureCnt);
        } catch (RuntimeException e) {
            /*
             * 대상 목록 조회처럼 개별 대상 처리 이전에 발생한 예외도 실행 실패로 남겨 관리자 화면에서 확인할 수 있게 한다.
             * 최종 마스터 로그를 갱신한 뒤 Spring 스케줄러에도 실패가 전달되도록 원래 예외를 다시 던진다.
             */
            failureCnt++;
            executionStatus = Constant.SCHEDULER_EXEC_FAILURE;
            setSchedulerFailSafely(
                    runxNumb
                  , Constant.SCHEDULER_FAIL_EXCEPTION
                  , null
                  , null
                  , e
            );
            log.error("목표 독서기간 초과 스케줄러 실행 중 오류가 발생했습니다.", e);
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
            uptSchedulerLogSafely(schedulerRunDto);

            log.info(
                    "목표 독서기간 초과 스케줄러가 종료되었습니다. 조회 건수={}, 성공 건수={}, 실패 건수={}, 최대 조회 건수={}"
                  , targetCnt
                  , successCnt
                  , failureCnt
                  , maxSize
            );
        }
    }

    /**
     * 실행 시작 로그를 등록하되 로그 저장 오류가 원래 스케줄러 업무를 중단시키지 않도록 격리합니다.
     *
     * @param schedulerRunDto 실행 시작 정보
     * @return 등록된 실행 번호, 로그 등록 실패 시 null
     */
    private Long setSchedulerLogSafely(SchedulerLogDto.SchedulerRunDto schedulerRunDto) {
        try {
            return schedulerLogService.setSchedulerLog(schedulerRunDto);
        } catch (RuntimeException e) {
            log.error("스케줄러 실행 시작 로그를 등록하지 못했습니다.", e);
            return null;
        }
    }

    /**
     * 실패 로그를 등록하되 로그 저장 오류가 다음 알림 대상의 처리를 막지 않도록 격리합니다.
     *
     * @param runxNumb 스케줄러 실행 번호
     * @param failType 실패 유형
     * @param resultCode 업무 처리 결과 코드
     * @param resultMessage 업무 처리 결과 메시지
     * @param exception 발생한 Java 예외
     */
    private void setSchedulerFailSafely(
            Long runxNumb
          , String failType
          , Integer resultCode
          , String resultMessage
          , RuntimeException exception) {

        // 마스터 로그 등록에 실패했다면 연결할 실행 번호가 없으므로 실패 상세 저장만 생략한다.
        if (runxNumb == null) {
            return;
        }

        SchedulerLogDto.SchedulerFailDto schedulerFailDto =
                new SchedulerLogDto.SchedulerFailDto();
        schedulerFailDto.setRunxNumb(runxNumb);
        schedulerFailDto.setFailType(failType);
        schedulerFailDto.setRsltCode(resultCode);
        schedulerFailDto.setRsltMesg(resultMessage);

        // 비정상 응답과 달리 Java 예외에는 예외 클래스와 메시지를 별도 컬럼에 보관한다.
        if (exception != null) {
            schedulerFailDto.setErroType(exception.getClass().getName());
            schedulerFailDto.setErroCntn(exception.getMessage());
        }

        try {
            schedulerLogService.setSchedulerFail(schedulerFailDto);
        } catch (RuntimeException e) {
            log.error("스케줄러 실패 상세 로그를 등록하지 못했습니다. 실행 번호={}", runxNumb, e);
        }
    }

    /**
     * 실행 종료 로그를 수정하되 로그 수정 오류가 스케줄러의 원래 처리 결과를 덮어쓰지 않도록 격리합니다.
     *
     * @param schedulerRunDto 실행 종료 정보
     */
    private void uptSchedulerLogSafely(SchedulerLogDto.SchedulerRunDto schedulerRunDto) {
        // 시작 로그가 등록되지 않았다면 수정할 마스터 행이 없으므로 종료 갱신을 생략한다.
        if (schedulerRunDto.getRunxNumb() == null) {
            return;
        }

        try {
            schedulerLogService.uptSchedulerLog(schedulerRunDto);
        } catch (RuntimeException e) {
            log.error(
                    "스케줄러 실행 종료 로그를 수정하지 못했습니다. 실행 번호={}"
                  , schedulerRunDto.getRunxNumb()
                  , e
            );
        }
    }

    /**
     * 성공 및 실패 건수로 마스터 로그의 최종 실행 상태를 결정합니다.
     *
     * @param successCnt 성공 건수
     * @param failureCnt 실패 건수
     * @return 성공, 일부 실패, 실패 중 하나의 실행 상태
     */
    private String getSchedulerExecutionStatus(
            int successCnt
          , int failureCnt) {

        // 실패가 한 건도 없으면 조회된 모든 대상이 성공한 상태이다.
        if (failureCnt == 0) {
            return Constant.SCHEDULER_EXEC_SUCCESS;
        }

        // 성공과 실패가 함께 있으면 관리자가 일부 대상만 재확인할 수 있도록 일부 실패로 구분한다.
        if (successCnt > 0) {
            return Constant.SCHEDULER_EXEC_PARTIAL;
        }

        return Constant.SCHEDULER_EXEC_FAILURE;
    }
}
