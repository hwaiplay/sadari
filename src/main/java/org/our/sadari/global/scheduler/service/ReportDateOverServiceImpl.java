package org.our.sadari.global.scheduler.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.alim.service.AlimService;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.scheduler.common.SchedulerLogSupport;
import org.our.sadari.global.scheduler.dto.SchedulerLogDto;
import org.our.sadari.global.scheduler.mapper.ReportDateOverMapper;
import org.our.sadari.report.dto.ReportDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : ReportDateOverServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-26
 * description    : 스케줄러 업무 로직을 구현한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-26        SeungHyeon.Kang    최초 생성
 */
@Service
@Slf4j
@Transactional(readOnly = true)
public class ReportDateOverServiceImpl implements ReportDateOverService {

    // 결과 성공 코드 설정값
    private static final int RESULT_SUCCESS_CODE = 200;
    // 빈 값 결과 메시지 설정값
    private static final String EMPTY_RESULT_MESSAGE = "알림 서비스 응답이 없습니다.";

    // ReportDateOver 데이터 접근 객체
    private final ReportDateOverMapper reportDateOverMapper;
    // Alim 업무 처리 서비스
    private final AlimService alimService;
    // 스케줄러 로그 안전 처리 객체
    private final SchedulerLogSupport schedulerLogSupport;
    // 스케줄러 1회 최대 처리 건수
    private final int maxSize;

    /**
     * 환경별 yml의 scheduler.max-size를 주입받아 한 번에 조회할 대상 수를 결정
     * 별도 설정 객체를 만들지 않고 단일 숫자 설정을 생성자에서 직접 주입해 테스트에서도 같은 제한값을 명시할 수 있음
     *
     * @author SeungHyeon.Kang
     * @param reportDateOverMapper 목표기간 초과 대상을 제한 조회하는 Mapper
     * @param alimService 알림 저장과 FCM 푸시 발송을 담당하는 서비스
     * @param schedulerLogSupport 로그 저장 예외를 격리하고 실행 상태를 판정하는 공통 컴포넌트
     * @param maxSize 한 번의 실행에서 조회할 최대 대상 수
     */
    public ReportDateOverServiceImpl(ReportDateOverMapper reportDateOverMapper, AlimService alimService, SchedulerLogSupport schedulerLogSupport
                                   , @Value("${scheduler.max-size}") int maxSize) {

        this.reportDateOverMapper = reportDateOverMapper;
        this.alimService = alimService;
        this.schedulerLogSupport = schedulerLogSupport;
        this.maxSize = maxSize;
    }

    /**
     * yml에 설정한 최대 건수만큼 대상을 조회하고 각 대상에게 목표기간 초과 알림을 발송
     * 한 대상의 발송 실패가 나머지 대상을 중단시키지 않도록 대상 단위로 예외를 격리
     *
     * @author SeungHyeon.Kang
     */
    @Override
    public void sendReportDateOverAlim() {
        // 실행 시간을 측정할 시작 시각을 기록한다
        long startNanoTime = System.nanoTime();
        // 스케줄러 실행 로그를 담을 객체를 생성한다
        SchedulerLogDto.SchedulerRunDto schedulerRunDto = new SchedulerLogDto.SchedulerRunDto();
        // SchdCode 업무 값을 schedulerRunDto DTO에 설정한다
        schedulerRunDto.setSchdCode(Constant.SCHEDULER_CODE_REPORT_DATE_OVER);

        /*
         * 공통 유틸리티에서 stack index를 계산하면 호출 깊이에 따라 다른 메서드명이 저장될 수 있다.
         * 실제 스케줄러 업무 메서드 안에서 현재 Thread의 두 번째 stack frame을 읽어 sendReportDateOverAlim을 기록한다.
         */
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
            // ReportDateOverTargetList 데이터를 DB에서 조회한다
            List<ReportDto> targetList = reportDateOverMapper.getOverdueReportList(maxSize);

            // MyBatis는 일반적으로 빈 List를 반환하지만 비정상 null 반환도 대상 없음으로 보정해 NPE를 차단한다.
            if (StringUtil.isEmpty(targetList) || targetList.isEmpty()) {

                executionStatus = Constant.SCHEDULER_EXEC_NO_DATA;
                // 진단에 필요한 처리 상태를 디버그 로그로 남긴다
                log.debug("목표 독서기간 초과 알림 대상이 없습니다. 최대 조회 건수={}", maxSize);
                // yml에 설정한 최대 건수만큼 대상을 조회하고 각 대상에게 목표기간 초과 알림을 발송 결과를 반환한다
                return;
            }

            // 처리된 데이터 건수를 확인한다
            targetCnt = targetList.size();

            // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
            for (ReportDto target : targetList) {
                // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
                try {
                    /*
                     * Mapper가 ReportDto에 독후감 번호, 사용자 번호, 책 제목을 함께 담아 반환한다.
                     * 조회한 제목을 그대로 치환 Map에 사용하므로 알림 발송 과정에서 도서 정보를 다시 조회하지 않는다.
                     */
                    ResultData result = alimService.sendAlim(
                            // getUserNumb 조회로 후속 처리에 필요한 데이터를 가져온다
                            target.getUserNumb()
                          , Constant.ALIM_SITU_REPORT
                          , Constant.ALIM_TEMP_CODE_REPORT_DATE_OVER
                          , Constant.LIKE_TARGET_REPORT
                          , target.getReptNumb()
                          , null
                          , Map.of("bookTitl", target.getBookTitl())
                    );

                    // ResultData가 존재하고 성공 코드인 경우에만 성공 건수로 집계한다.
                    if (!StringUtil.isEmpty(result) && result.getCode() == RESULT_SUCCESS_CODE) {

                        successCnt++;
                        continue;
                    }

                    failureCnt++;
                    // 필수 값이 비어 있는지 공통 기준으로 확인한다
                    Integer resultCode = StringUtil.isEmpty(result) ? null : result.getCode();
                    // 필수 값이 비어 있는지 공통 기준으로 확인한다
                    String resultMessage = StringUtil.isEmpty(result) ? EMPTY_RESULT_MESSAGE : result.getMessage();
                    // FailCntt 업무 값을 schedulerRunDto DTO에 설정한다
                    schedulerRunDto.setFailCntt(failureCnt);
                    // TrgtCntt 업무 값을 schedulerRunDto DTO에 설정한다
                    schedulerRunDto.setTrgtCntt(targetCnt);
                    // 실패 상세를 연결할 실행 번호가 없으면 최초 실패 시점에 마스터 로그를 생성한다.
                    if (StringUtil.isEmpty(runxNumb)) {
                        // SchedulerLogSafely 업무 값을 schedulerLogSupport DTO에 설정한다
                        runxNumb = schedulerLogSupport.setSchedulerLogSafely(schedulerRunDto);
                    }

                    // SchedulerFailSafely 업무 값을 schedulerLogSupport DTO에 설정한다
                    schedulerLogSupport.setSchedulerFailSafely(
                            runxNumb
                          , Constant.SCHEDULER_FAIL_REJECTED
                          , resultCode
                          , resultMessage
                          , null
                    );

                    // 복구 가능한 예외 상황을 경고 로그로 남긴다
                    log.warn("목표 독서기간 초과 알림 발송이 거부되었습니다. 사용자 번호={}, 독후감 번호={}, 응답 코드={}"
                           , target.getUserNumb(), target.getReptNumb(), resultCode);
                }

                // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
                catch (RuntimeException e) {
                    /*
                     * 한 대상의 예외가 남은 배치를 중단시키지 않도록 실패 건만 기록하고 다음 대상으로 진행한다.
                     * 저장되지 않은 대상은 조회 SQL의 NOT EXISTS 조건을 계속 만족하므로 다음 실행에서 다시 시도된다.
                     */
                    failureCnt++;
                    // FailCntt 업무 값을 schedulerRunDto DTO에 설정한다
                    schedulerRunDto.setFailCntt(failureCnt);
                    // TrgtCntt 업무 값을 schedulerRunDto DTO에 설정한다
                    schedulerRunDto.setTrgtCntt(targetCnt);
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
                    log.error("목표 독서기간 초과 알림 발송 중 오류가 발생했습니다. 사용자 번호={}, 독후감 번호={}", target.getUserNumb(), target.getReptNumb(), e);
                }
            }

            // getSchedulerExecStatus 조회로 후속 처리에 필요한 데이터를 가져온다
            executionStatus = schedulerLogSupport.getSchedulerExecStatus(successCnt, failureCnt);
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (RuntimeException e) {
            /*
             * 대상 목록 조회처럼 개별 대상 처리 이전에 발생한 예외도 실행 실패로 남겨 관리자 화면에서 확인할 수 있게 한다.
             * 최종 마스터 로그를 갱신한 뒤 Spring 스케줄러에도 실패가 전달되도록 원래 예외를 다시 던진다.
             */
            failureCnt++;
            executionStatus = Constant.SCHEDULER_EXEC_FAILURE;
            // FailCntt 업무 값을 schedulerRunDto DTO에 설정한다
            schedulerRunDto.setFailCntt(failureCnt);
            // 실행 전반의 예외를 연결할 실행 번호가 없으면 마스터 로그를 생성한다.
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
            log.error("목표 독서기간 초과 스케줄러 실행 중 오류가 발생했습니다.", e);
            throw e;
        }

        // 성공 여부와 관계없이 반드시 자원을 정리하기 위한 블록이다
        finally {

            schedulerRunDto.setRunxNumb(runxNumb);
            schedulerRunDto.setExecStat(executionStatus);
            schedulerRunDto.setTrgtCntt(targetCnt);
            schedulerRunDto.setSuccCntt(successCnt);
            schedulerRunDto.setFailCntt(failureCnt);
            schedulerRunDto.setExecMsec(
                    // 측정한 실행 시간을 밀리초 단위로 변환한다
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanoTime)
            );
            // 처리 결과가 존재하지만 실패가 없어 마스터 로그를 만들지 않았다면 최종 건수로 로그를 생성한다.
            if (StringUtil.isEmpty(runxNumb)) {
                // SchedulerLogSafely 업무 값을 schedulerLogSupport DTO에 설정한다
                runxNumb = schedulerLogSupport.setSchedulerLogSafely(schedulerRunDto);
                // RunxNumb 업무 값을 schedulerRunDto DTO에 설정한다
                schedulerRunDto.setRunxNumb(runxNumb);
            }

            // uptSchedulerLogSafely 호출로 변경된 업무 상태를 반영한다
            schedulerLogSupport.uptSchedulerLogSafely(schedulerRunDto);

            // 처리 상태를 정보 로그로 남긴다
            log.info("목표 독서기간 초과 스케줄러가 종료되었습니다. 조회 건수={}, 성공 건수={}, 실패 건수={}, 최대 조회 건수={}"
                   , targetCnt, successCnt, failureCnt
                   , maxSize);
        }
    }

}
