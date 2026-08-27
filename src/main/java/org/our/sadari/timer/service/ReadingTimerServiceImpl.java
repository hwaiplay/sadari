package org.our.sadari.timer.service;

import lombok.extern.slf4j.Slf4j;
import org.our.sadari.alim.service.AlimService;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.dto.PageDto;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.scheduler.common.SchedulerLogSupport;
import org.our.sadari.global.scheduler.dto.SchedulerLogDto;
import org.our.sadari.timer.config.ReadingTimerProperties;
import org.our.sadari.timer.dto.ReadingTimerDto;
import org.our.sadari.timer.mapper.ReadingTimerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * fileName       : ReadingTimerServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-08-14
 * description    : 독서 세션과 주간 출석 및 목표시간 자동 완료 업무를 처리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        SeungHyeon.Kang    최초 생성 및 완료 타이머 처리
 * 2026-08-20        SeungHyeon.Kang    목표시간 알림·도서별 누적 페이지 조회 통합
 * 2026-08-21        SeungHyeon.Kang    목표시간 종료 자동 완료 및 알림 재시도
 */
@Service
@Slf4j
@Transactional(readOnly = true)
public class ReadingTimerServiceImpl implements ReadingTimerService {

    // 공통 성공 응답 코드
    private static final int RESULT_SUCCESS_CODE = 200;
    // 도서별 누적 독서 시간 한 페이지 표시 건수
    private static final int BOOK_TIME_PAGE_SIZE = 20;

    // 독서 타이머 데이터 접근 객체
    private final ReadingTimerMapper readingTimerMapper;
    // 독서 타이머 운영 기준
    private final ReadingTimerProperties properties;
    // 알림 저장과 푸시 발송 업무 서비스
    private final AlimService alimService;
    // 스케줄러 로그 안전 처리 객체
    private final SchedulerLogSupport schedulerLogSupport;
    // 대상 세션별 신규 트랜잭션 실행 객체
    private final TransactionTemplate timerTransactionTemplate;
    // 스케줄러 1회 최대 처리 건수
    private final int maxSize;
    // 서버 현재 일시를 제공하는 시계
    private final Clock clock;
    // 일별 출석 경계를 계산할 시간대
    private final ZoneId zoneId;

    /**
     * 운영 시간대를 기준으로 독서 타이머 서비스를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param readingTimerMapper 독서 타이머 데이터 접근 객체
     * @param properties 독서 타이머 운영 기준
     * @param alimService 알림 저장과 푸시 발송 업무 서비스
     * @param schedulerLogSupport 스케줄러 로그 안전 처리 객체
     * @param transactionManager 대상 세션별 트랜잭션 관리자
     * @param maxSize 한 번에 조회할 최대 건수
     */
    @Autowired
    public ReadingTimerServiceImpl(ReadingTimerMapper readingTimerMapper, ReadingTimerProperties properties, AlimService alimService
                                 , SchedulerLogSupport schedulerLogSupport, PlatformTransactionManager transactionManager
                                 , @Value("${scheduler.max-size:100}") int maxSize) {

        this(readingTimerMapper, properties, alimService, schedulerLogSupport, transactionManager
           , Clock.system(ZoneId.of(properties.getZoneId())), maxSize);
    }

    /**
     * 테스트에서 고정 시계를 사용할 수 있도록 독서 타이머 서비스를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param readingTimerMapper 독서 타이머 데이터 접근 객체
     * @param properties 독서 타이머 운영 기준
     * @param alimService 알림 저장과 푸시 발송 업무 서비스
     * @param schedulerLogSupport 스케줄러 로그 안전 처리 객체
     * @param transactionManager 대상 세션별 트랜잭션 관리자
     * @param clock 현재 일시 제공 시계
     * @param maxSize 한 번에 조회할 최대 건수
     */
    ReadingTimerServiceImpl(ReadingTimerMapper readingTimerMapper, ReadingTimerProperties properties, AlimService alimService
                          , SchedulerLogSupport schedulerLogSupport, PlatformTransactionManager transactionManager
                          , Clock clock, int maxSize) {

        this.readingTimerMapper = readingTimerMapper;
        this.properties = properties;
        this.alimService = alimService;
        this.schedulerLogSupport = schedulerLogSupport;
        // 대상 세션마다 독립된 커밋과 롤백을 적용할 트랜잭션 실행 객체를 생성한다
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        // 한 세션 실패가 다른 세션 처리에 영향을 주지 않도록 신규 트랜잭션 전파를 설정한다
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.timerTransactionTemplate = transactionTemplate;
        this.maxSize = maxSize;
        this.clock = clock;
        this.zoneId = ZoneId.of(properties.getZoneId());
    }

    /**
     * 로그인 사용자의 현재 타이머와 이번 주 출석 현황을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 타이머 화면 요약 데이터
     */
    @Override
    public ResultData getTimerSummary(Long userNumb) {

        // 조회 시점의 서버 시간을 한 번만 고정하여 응답 내 시간 기준을 일치시킨다
        LocalDateTime now = getNow();
        // 현재 타이머와 주간 출석 현황을 조합해 반환한다
        return ResultData.success(getSummary(userNumb, now));
    }

    /**
     * 로그인 사용자의 도서별 누적 독서 시간을 최근 기록순으로 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param page 조회할 페이지 번호
     * @return 현재 페이지 도서별 누적시간과 다음 페이지 여부
     */
    @Override
    public ResultData getBookTimePage(Long userNumb, int page) {

        // 인증 사용자 번호가 없으면 다른 사용자의 타이머 기록을 조회하지 않는다
        if (StringUtil.isEmpty(userNumb)) {
            // 인증 실패 공통 응답을 반환한다
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 요청 페이지를 첫 페이지 이상으로 보정한다
        int normalizedPage = Math.max(page, 1);
        // 현재 페이지의 시작 위치를 계산한다
        int pageOffset = (normalizedPage - 1) * BOOK_TIME_PAGE_SIZE;
        // 다음 페이지 판정용 한 건을 더해 도서별 누적시간을 조회한다
        List<ReadingTimerDto.BookTime> searchedList = readingTimerMapper.getBookTimeList(
                userNumb
              , Constant.TIMER_STAT_COMPLETED
              , pageOffset
              , BOOK_TIME_PAGE_SIZE + 1
        );
        // Mapper가 빈 값을 반환해도 페이지 응답을 유지하도록 빈 목록으로 보정한다
        List<ReadingTimerDto.BookTime> safeList = StringUtil.isEmpty(searchedList) ? List.of() : searchedList;
        // 표시 건수보다 한 건 더 조회됐는지 다음 페이지 여부로 판정한다
        boolean hasNext = safeList.size() > BOOK_TIME_PAGE_SIZE;
        // 화면에는 현재 페이지 크기인 최대 20건만 전달한다
        List<ReadingTimerDto.BookTime> visibleList = hasNext
                ? safeList.subList(0, BOOK_TIME_PAGE_SIZE)
                : safeList;
        // 현재 페이지 목록과 다음 페이지 여부를 공통 페이지 응답으로 반환한다
        return ResultData.success(new PageDto<>(visibleList, normalizedPage, hasNext));
    }

    /**
     * 중복 실행 요청을 흡수하며 새 독서 타이머를 시작한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param request 시작 요청 정보
     * @return 시작 후 타이머 화면 요약 데이터
     */
    @Override
    @Transactional
    public ResultData setTimer(Long userNumb, ReadingTimerDto.Request request) {

        // 같은 사용자의 동시 시작 요청을 사용자 행 잠금으로 직렬화한다
        readingTimerMapper.getUserLock(userNumb);
        // 이미 완료되지 않은 세션이 있으면 중복 생성 없이 현재 화면 데이터를 반환한다
        ReadingTimerDto activeTimer = getActiveTimer(userNumb);
        if (!StringUtil.isEmpty(activeTimer)) {
            // 기존 세션을 유지한 요약 데이터를 반환한다
            return ResultData.success(getSummary(userNumb, getNow()));
        }
        // 연결 도서를 선택했다면 로그인 사용자의 읽는 중 독후감인지 검증한다
        if (!StringUtil.isEmpty(request) && !StringUtil.isEmpty(request.getReptNumb())
                && readingTimerMapper.getReadingReportCnt(userNumb, request.getReptNumb(), Constant.REPORT_STAT_READ) == Constant.NUMBER_ZERO) {
            // 타이머에 연결할 수 없는 도서 안내를 반환한다
            return ResultData.fail(ResultEnum.TIMER_BOOK_INVALID);
        }
        // 알림 목표시간을 설정했다면 단일 세션 최대시간 안의 양수인지 검증한다
        if (!StringUtil.isEmpty(request) && !StringUtil.isEmpty(request.getTargSecs())
                && !isTargetSeconds(request.getTargSecs())) {
            // 허용 범위를 벗어난 목표시간 안내를 반환한다
            return ResultData.fail(ResultEnum.TIMER_TARGET_INVALID);
        }

        // 새 세션에 동일한 서버 시작 시각을 적용한다
        LocalDateTime now = getNow();
        ReadingTimerDto timerDto = new ReadingTimerDto();
        // 로그인 사용자 번호를 새 세션에 설정한다
        timerDto.setUserNumb(userNumb);
        // 요청한 독후감 번호를 새 세션에 설정한다
        timerDto.setReptNumb(StringUtil.isEmpty(request) ? null : request.getReptNumb());
        // 새 세션을 실행 중 상태로 설정한다
        timerDto.setTmrxStat(Constant.TIMER_STAT_RUNNING);
        // 요청한 알림 목표시간을 새 세션에 설정한다
        timerDto.setTargSecs(StringUtil.isEmpty(request) ? null : request.getTargSecs());
        // 확정 독서 시간을 0초로 설정한다
        timerDto.setReadSecs(Constant.NUMBER_ZERO);
        // 목표시간이 있으면 최초 시작 시각 기준 알림 예정 일시를 설정한다
        timerDto.setAlrmDate(StringUtil.isEmpty(timerDto.getTargSecs()) ? null : now.plusSeconds(timerDto.getTargSecs()));
        // 새 세션에는 알림 발송 일시가 없다
        timerDto.setSendDate(null);
        // 세션 최초 시작 일시를 설정한다
        timerDto.setStrtDate(now);
        // 현재 측정 구간 시작 일시를 설정한다
        timerDto.setLastStrt(now);
        // 등록 일시를 설정한다
        timerDto.setRegiDate(now);
        // 수정 일시를 설정한다
        timerDto.setUpdtDate(now);
        // 새 독서 타이머 세션을 등록한다
        readingTimerMapper.setTimer(timerDto);
        // 시작 결과가 반영된 화면 요약을 반환한다
        return ResultData.success(getSummary(userNumb, now));
    }

    /**
     * 실행 중인 타이머를 재개, 일시정지 또는 완료 처리한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param tmrxNumb 변경할 세션 번호
     * @param request 변경할 상태 정보
     * @return 변경 후 타이머 화면 요약 데이터
     */
    @Override
    @Transactional
    public ResultData uptTimer(Long userNumb, Long tmrxNumb, ReadingTimerDto.Request request) {

        // 같은 사용자의 상태 전환과 시간 누적을 한 번에 처리하도록 사용자 행을 잠근다
        readingTimerMapper.getUserLock(userNumb);
        // 사용자 소유 세션을 조회한다
        ReadingTimerDto timerDto = readingTimerMapper.getTimerDtl(userNumb, tmrxNumb);
        if (StringUtil.isEmpty(timerDto)) {
            // 찾을 수 없는 독서 타이머 안내를 반환한다
            return ResultData.fail(ResultEnum.TIMER_SESSION_NOT_FOUND);
        }
        // 요청 상태가 허용된 상태인지 검증한다
        String targetStat = StringUtil.isEmpty(request) ? null : request.getTmrxStat();
        if (!isTimerStat(targetStat)) {
            // 허용되지 않은 상태 전환 안내를 반환한다
            return ResultData.fail(ResultEnum.TIMER_STATE_INVALID);
        }
        // 상태 전환 시점의 서버 시간을 고정한다
        LocalDateTime now = getNow();
        // 목표시간이 지난 실행 세션은 요청 상태와 관계없이 목표시각에 완료한다
        if (isTimerTargetOver(timerDto, now)) {
            // 목표시간 이후의 지연 구간을 제외하고 세션을 완료 상태로 저장한다
            uptTimerTargetOver(timerDto, now);
            // 자동 완료 결과가 반영된 최신 타이머 화면을 반환한다
            return ResultData.success(getSummary(userNumb, now));
        }
        // 같은 상태로 재요청한 경우 중복 누적 없이 최신 요약을 반환한다
        if (targetStat.equals(timerDto.getTmrxStat())) {
            // 멱등 처리된 최신 타이머 화면을 반환한다
            return ResultData.success(getSummary(userNumb, getNow()));
        }
        // 완료된 세션은 다른 상태로 되돌릴 수 없다
        if (Constant.TIMER_STAT_COMPLETED.equals(timerDto.getTmrxStat())) {
            // 완료 세션 상태 변경 불가 안내를 반환한다
            return ResultData.fail(ResultEnum.TIMER_STATE_INVALID);
        }

        // 실행 중 세션을 닫을 때 현재 구간을 일별 집계에 확정한다
        if (Constant.TIMER_STAT_RUNNING.equals(timerDto.getTmrxStat())) {
            // 최근 시작부터 현재까지의 유효 구간을 확정한다
            closeRunningSegment(timerDto, now);
        }
        // 일시정지에서 실행 중으로 재개할 때 새 구간 시작 시각을 설정한다
        if (Constant.TIMER_STAT_RUNNING.equals(targetStat)) {
            // 재개한 측정 구간 시작 시각을 설정한다
            timerDto.setLastStrt(now);
            // 완료 일시를 비운다
            timerDto.setEndxDate(null);
            // 아직 발송하지 않은 목표 알림을 남은 독서시간 기준으로 다시 예약한다
            timerDto.setAlrmDate(getAlarmDate(timerDto, now));
        } else {
            // 측정하지 않는 상태에서는 최근 시작 시각을 비운다
            timerDto.setLastStrt(null);
            // 완료 상태일 때만 완료 일시를 기록한다
            timerDto.setEndxDate(Constant.TIMER_STAT_COMPLETED.equals(targetStat) ? now : null);
            // 일시정지와 완료 상태에서는 예약 알림을 해제한다
            timerDto.setAlrmDate(null);
        }
        // 요청한 상태를 세션에 설정한다
        timerDto.setTmrxStat(targetStat);
        // 최종 수정 일시를 설정한다
        timerDto.setUpdtDate(now);
        // 확정된 세션 상태와 시간을 저장한다
        readingTimerMapper.uptTimer(timerDto);
        // 변경 결과가 반영된 화면 요약을 반환한다
        return ResultData.success(getSummary(userNumb, now));
    }

    /**
     * 목표시간 자동 완료와 알림 대상별 신규 트랜잭션을 실행하고 결과 로그를 저장한다
     *
     * @author SeungHyeon.Kang
     */
    @Override
    public void sendTimerAlim() {

        long startNanoTime = System.nanoTime();
        LocalDateTime alarmDate = getNow();
        // 독서 타이머 목표시간 알림 실행 정보를 담을 객체를 생성한다
        SchedulerLogDto.SchedulerRunDto schedulerRunDto = new SchedulerLogDto.SchedulerRunDto();
        // 스케줄러 식별 코드를 설정한다
        schedulerRunDto.setSchdCode(Constant.SCHEDULER_CODE_BOOK_TIMER_OVER);
        // 실행 메서드 이름을 설정한다
        schedulerRunDto.setMethName(Thread.currentThread().getStackTrace()[1].getMethodName());
        // 실행 시작 상태를 설정한다
        schedulerRunDto.setExecStat(Constant.SCHEDULER_EXEC_RUNNING);
        // 실행 시작 일시를 설정한다
        schedulerRunDto.setStrtDate(alarmDate);
        Long runxNumb = null;
        int targetCnt = 0;
        int successCnt = 0;
        int failureCnt = 0;
        String executionStatus = Constant.SCHEDULER_EXEC_RUNNING;

        // 한 주기의 대상 조회와 건별 발송 실패를 스케줄러 실행 결과로 집계한다
        try {
            // 목표시간이 지난 실행 세션과 알림 재시도 세션을 최대 처리 건수 안에서 조회한다
            List<Long> targetList = readingTimerMapper.getDueTimerAlimList(Constant.TIMER_STAT_RUNNING
                                                                         , Constant.TIMER_STAT_COMPLETED
                                                                         , Constant.USER_STAT_ACTIVE, alarmDate, maxSize);
            // 발송 대상이 없으면 실행 로그 없이 종료한다
            if (StringUtil.isEmpty(targetList) || targetList.isEmpty()) {
                // 대상 없음 상태를 설정한다
                executionStatus = Constant.SCHEDULER_EXEC_NO_DATA;
                // 독서 타이머 목표시간 알림 배치를 종료한다
                return;
            }

            targetCnt = targetList.size();
            // 조회한 세션을 대상 단위 트랜잭션으로 순차 처리한다
            for (Long tmrxNumb : targetList) {
                // 한 대상 실패가 다음 세션 발송을 막지 않도록 예외를 건별로 격리한다
                try {
                    // 목표시각까지의 독서시간과 완료 상태를 알림과 독립된 신규 트랜잭션으로 저장한다
                    timerTransactionTemplate.executeWithoutResult(transactionStatus -> uptTimerOverTarget(tmrxNumb, alarmDate));
                    // 알림 저장 실패가 자동 완료를 되돌리지 않도록 후속 신규 트랜잭션에서 처리한다
                    timerTransactionTemplate.executeWithoutResult(transactionStatus -> sendTimerAlimTarget(tmrxNumb, alarmDate));
                    successCnt++;
                }

                // 대상 세션 처리 예외를 기록하고 나머지 발송을 계속한다
                catch (RuntimeException e) {
                    failureCnt++;
                    // 현재 실패 건수를 실행 로그에 설정한다
                    schedulerRunDto.setFailCntt(failureCnt);
                    // 전체 대상 건수를 실행 로그에 설정한다
                    schedulerRunDto.setTrgtCntt(targetCnt);
                    // 최초 실패 시 마스터 로그를 생성한다
                    if (StringUtil.isEmpty(runxNumb)) {
                        // 실패 상세를 연결할 실행 번호를 생성한다
                        runxNumb = schedulerLogSupport.setSchedulerLogSafely(schedulerRunDto);
                    }

                    // 대상 단위 예외 상세를 저장한다
                    schedulerLogSupport.setSchedulerFailSafely(runxNumb, Constant.SCHEDULER_FAIL_EXCEPTION
                                                              , null, null, e);
                    // 다음 대상 처리를 계속할 수 있도록 실패 세션을 로그로 남긴다
                    log.error("독서 타이머 목표시간 자동 완료 또는 알림 발송 중 오류가 발생했습니다. 세션 번호={}", tmrxNumb, e);
                }
            }

            // 성공과 실패 건수로 최종 실행 상태를 계산한다
            executionStatus = schedulerLogSupport.getSchedulerExecStatus(successCnt, failureCnt);
        }

        // 배치 대상 조회 자체가 실패하면 실행 실패 이력을 남기고 호출 계층에 예외를 전달한다
        catch (RuntimeException e) {
            failureCnt++;
            executionStatus = Constant.SCHEDULER_EXEC_FAILURE;
            // 배치 예외 건수를 실행 로그에 설정한다
            schedulerRunDto.setFailCntt(failureCnt);
            // 마스터 로그가 없으면 생성한다
            if (StringUtil.isEmpty(runxNumb)) {
                // 실패 상세를 연결할 실행 번호를 생성한다
                runxNumb = schedulerLogSupport.setSchedulerLogSafely(schedulerRunDto);
            }

            // 배치 조회 또는 처리 예외 상세를 저장한다
            schedulerLogSupport.setSchedulerFailSafely(runxNumb, Constant.SCHEDULER_FAIL_EXCEPTION
                                                      , null, null, e);
            // 스케줄러 실행 실패를 운영 로그에 남긴다
            log.error("독서 타이머 목표시간 자동 완료 스케줄러 실행 중 오류가 발생했습니다.", e);
            throw e;
        }

        // 대상 또는 실패가 있는 실행만 운영 이력에 남겨 빈 로그 누적을 막는다
        finally {
            // 실제 처리 결과가 있는 실행만 종료 로그를 구성한다
            if (targetCnt > 0 || failureCnt > 0) {
                // 실행 번호를 최종 로그에 설정한다
                schedulerRunDto.setRunxNumb(runxNumb);
                // 최종 실행 상태를 설정한다
                schedulerRunDto.setExecStat(executionStatus);
                // 전체 대상 건수를 설정한다
                schedulerRunDto.setTrgtCntt(targetCnt);
                // 성공 건수를 설정한다
                schedulerRunDto.setSuccCntt(successCnt);
                // 실패 건수를 설정한다
                schedulerRunDto.setFailCntt(failureCnt);
                // 실행 소요시간을 밀리초로 설정한다
                schedulerRunDto.setExecMsec(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanoTime));
                // 처리 결과가 있는데 마스터 로그가 없으면 최종 로그를 생성한다
                if (StringUtil.isEmpty(runxNumb)) {
                    // 최종 건수로 실행 로그를 생성한다
                    runxNumb = schedulerLogSupport.setSchedulerLogSafely(schedulerRunDto);
                    // 생성한 실행 번호를 최종 로그에 설정한다
                    schedulerRunDto.setRunxNumb(runxNumb);
                }

                // 실행 종료 상태와 건수를 반영한다
                schedulerLogSupport.uptSchedulerLogSafely(schedulerRunDto);
            }
        }
    }

    /**
     * 계정 상태 변경 직전에 실행 중인 독서 시간을 확정하고 타이머를 완료한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 상태를 변경할 사용자 번호
     */
    @Override
    @Transactional
    public void uptTimerWithdrawal(Long userNumb) {

        // 계정 상태 변경과 타이머 종료가 충돌하지 않도록 사용자 행을 잠근다
        readingTimerMapper.getUserLock(userNumb);
        // 계정 처리와 예약 취소에 동일한 서버 시각을 적용한다
        LocalDateTime now = getNow();
        ReadingTimerDto timerDto = getActiveTimer(userNumb);
        // 진행 또는 일시정지 세션이 있을 때만 계정 처리 시점에 완료한다
        if (!StringUtil.isEmpty(timerDto)) {
            // 계정 처리 시점까지 실행 중인 독서 시간을 확정한다
            if (Constant.TIMER_STAT_RUNNING.equals(timerDto.getTmrxStat())) {
                // 현재 실행 구간을 날짜별 집계에 반영한다
                closeRunningSegment(timerDto, now);
            }
            // 계정 상태 변경 이후 다시 실행되지 않도록 세션을 완료한다
            timerDto.setTmrxStat(Constant.TIMER_STAT_COMPLETED);
            // 최근 시작 시각을 비운다
            timerDto.setLastStrt(null);
            // 계정 처리 시점을 완료 일시로 설정한다
            timerDto.setEndxDate(now);
            // 계정 제한 이후 발송되지 않도록 예약 알림을 해제한다
            timerDto.setAlrmDate(null);
            // 수정 일시를 설정한다
            timerDto.setUpdtDate(now);
            // 완료된 세션 값을 저장한다
            readingTimerMapper.uptTimer(timerDto);
        }
        // 직전에 자동 완료된 세션을 포함해 아직 발송되지 않은 목표시간 예약을 모두 취소한다
        readingTimerMapper.uptTimerAlimCancel(userNumb, now);
    }

    /**
     * 보존기간이 지난 완료 세션 상세를 삭제한다
     *
     * @author SeungHyeon.Kang
     * @return 삭제한 세션 수
     */
    @Override
    @Transactional
    public int delExpiredTimer() {

        // 운영 보존기간 이전에 완료된 세션 상세를 삭제한다
        return readingTimerMapper.delExpiredTimer(Constant.TIMER_STAT_COMPLETED, getNow().minusDays(properties.getDetailRetentionDays()));
    }

    /**
     * 목표시간이 지난 실행 세션을 목표 종료시각 기준으로 완료한다
     *
     * @author SeungHyeon.Kang
     * @param tmrxNumb 독서 타이머 세션 번호
     * @param alarmDate 자동 완료 대상 조회 기준 일시
     */
    private void uptTimerOverTarget(Long tmrxNumb, LocalDateTime alarmDate) {

        // 신규 트랜잭션에서 자동 완료 조건을 다시 검증하며 실행 세션 행을 잠근다
        ReadingTimerDto timerDto = readingTimerMapper.getDueTimerAlimDtl(tmrxNumb, Constant.TIMER_STAT_RUNNING
                                                                       , Constant.USER_STAT_ACTIVE, alarmDate);
        // 다른 실행에서 먼저 완료했거나 상태가 바뀐 세션은 정상적으로 건너뛴다
        if (StringUtil.isEmpty(timerDto)) {
            // 자동 완료할 대상이 없는 정상 흐름을 종료한다
            return;
        }

        // 스케줄러 실행 지연 시간을 독서시간에 포함하지 않고 목표시각에 세션을 완료한다
        uptTimerTargetOver(timerDto, alarmDate);
    }

    /**
     * 완료 세션 행 잠금 안에서 알림과 발송 완료 일시를 함께 저장한다
     *
     * @author SeungHyeon.Kang
     * @param tmrxNumb 독서 타이머 세션 번호
     * @param alarmDate 발송 대상 조회 기준 일시
     */
    private void sendTimerAlimTarget(Long tmrxNumb, LocalDateTime alarmDate) {

        // 자동 완료 커밋 뒤 별도 트랜잭션에서 발송 조건을 다시 검증하며 세션 행을 잠근다
        ReadingTimerDto timerDto = readingTimerMapper.getTimerAlimDtl(tmrxNumb, Constant.TIMER_STAT_COMPLETED
                                                                    , Constant.USER_STAT_ACTIVE, alarmDate);
        // 다른 실행에서 먼저 발송했거나 상태가 바뀐 세션은 정상적으로 건너뛴다
        if (StringUtil.isEmpty(timerDto)) {
            // 처리할 대상이 없는 정상 흐름을 종료한다
            return;
        }

        // 세션별 목표시간을 템플릿 치환값으로 전달하여 공통 알림을 저장한다
        ResultData result = alimService.sendAlim(
              // 알림 수신 사용자 번호를 전달한다
                timerDto.getUserNumb()
              , Constant.ALIM_SITU_TIMER
              , Constant.ALIM_TEMP_CODE_BOOK_TIMER_OVER
              , Constant.ALIM_TARGET_TIMER
              , null
              , null
              , Map.of("timerTime", formatTimerTime(timerDto.getTargSecs()))
        );
        // 알림 저장이 거부되면 발송 일시를 남기지 않고 다음 실행에서 재시도한다
        if (StringUtil.isEmpty(result) || result.getCode() != RESULT_SUCCESS_CODE) {
            // 대상 단위 트랜잭션을 롤백하도록 예외를 발생시킨다
            throw new IllegalStateException("독서 타이머 목표시간 알림 발송이 거부되었습니다.");
        }

        // 알림 저장에 성공한 기준 일시를 발송 일시로 설정한다
        timerDto.setSendDate(alarmDate);
        // 예약 알림을 해제한다
        timerDto.setAlrmDate(null);
        // 세션 수정 일시를 발송 기준 일시로 설정한다
        timerDto.setUpdtDate(alarmDate);
        // 발송 완료 일시를 세션에 저장한다
        readingTimerMapper.uptTimerAlimSent(timerDto);
    }

    /**
     * 초 단위 목표시간을 알림 문구용 시·분 문자열로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param targetSeconds 목표 독서 시간 초
     * @return 알림 템플릿 치환 문자열
     */
    private String formatTimerTime(long targetSeconds) {

        long hours = targetSeconds / 3600L;
        long minutes = (targetSeconds % 3600L) / 60L;
        // 한 시간 미만 목표는 분 단위로 표시한다
        if (hours == 0L) {
            // 최소 설정 단위에 맞춘 분 문자열을 반환한다
            return Math.max(1L, minutes) + "분";
        }

        // 정각 목표는 시간만 표시한다
        if (minutes == 0L) {
            // 시간 단위 문자열을 반환한다
            return hours + "시간";
        }

        // 시간과 분이 모두 있는 목표 문자열을 반환한다
        return hours + "시간 " + minutes + "분";
    }

    /**
     * 타이머 화면에 필요한 데이터와 주간 출석을 조합한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param now 응답 계산 기준 서버 일시
     * @return 타이머 화면 요약 데이터
     */
    private ReadingTimerDto.Summary getSummary(Long userNumb, LocalDateTime now) {

        LocalDate today = now.toLocalDate();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6L);
        ReadingTimerDto activeTimer = getActiveTimer(userNumb);
        Map<LocalDate, Long> dailySeconds = new HashMap<>();
        // 저장된 주간 일별 집계를 날짜별 맵에 담는다
        for (ReadingTimerDto.Daily daily : readingTimerMapper.getDailyList(userNumb, weekStart, weekEnd)) {
            // 확정된 독서 시간을 해당 날짜에 설정한다
            dailySeconds.put(daily.getReadDate(), daily.getReadSecs());
        }
        // 실행 중인 미확정 구간을 응답 계산에만 임시 반영한다
        if (!StringUtil.isEmpty(activeTimer) && Constant.TIMER_STAT_RUNNING.equals(activeTimer.getTmrxStat())) {
            // 저장하지 않은 현재 구간을 주간 표시용으로 더한다
            addLiveSegment(activeTimer, now, dailySeconds);
        }

        List<ReadingTimerDto.Daily> weekList = new ArrayList<>();
        int attendanceCount = 0;
        // 월요일부터 일요일까지 빠진 날짜 없이 응답 목록을 만든다
        for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
            LocalDate readDate = weekStart.plusDays(dayIndex);
            long readSecs = dailySeconds.getOrDefault(readDate, 0L);
            ReadingTimerDto.Daily daily = new ReadingTimerDto.Daily();
            // 주간 목록 날짜를 설정한다
            daily.setReadDate(readDate);
            // 날짜별 독서 시간 초를 설정한다
            daily.setReadSecs(readSecs);
            // 최소 독서 시간을 충족했는지 설정한다
            daily.setAttended(readSecs >= properties.getAttendanceMinSeconds());
            // 오늘 날짜인지 설정한다
            daily.setToday(today.equals(readDate));
            // 출석한 날짜를 주간 출석 수에 반영한다
            if (daily.isAttended()) {
                attendanceCount++;
            }
            // 구성한 일별 출석을 주간 목록에 추가한다
            weekList.add(daily);
        }
        // 실행 중 세션 표시 시간에 현재 구간을 반영한다
        if (!StringUtil.isEmpty(activeTimer) && Constant.TIMER_STAT_RUNNING.equals(activeTimer.getTmrxStat())) {
            // 화면 카운터용 현재 누적 시간을 설정한다
            activeTimer.setReadSecs(getLiveTotal(activeTimer, now));
        }

        ReadingTimerDto.Summary summary = new ReadingTimerDto.Summary();
        // 현재 타이머를 설정한다
        summary.setActiveTimer(activeTimer);
        // 이번 주 시작일을 설정한다
        summary.setWeekStart(weekStart);
        // 이번 주 종료일을 설정한다
        summary.setWeekEnd(weekEnd);
        // 응답 기준 서버 일시를 설정한다
        summary.setServerDate(now);
        // 오늘 누적 독서 시간을 설정한다
        summary.setTodayReadSecs(dailySeconds.getOrDefault(today, 0L));
        // 출석 최소 시간을 설정한다
        summary.setAttendanceMinSecs(properties.getAttendanceMinSeconds());
        // 단일 세션 최대 시간을 설정한다
        summary.setMaxSessionSecs(properties.getMaxSessionSeconds());
        // 이번 주 출석 일수를 설정한다
        summary.setWeekAttendanceCount(attendanceCount);
        // 이번 주 일별 출석 목록을 설정한다
        summary.setWeekList(weekList);
        // 연결 가능한 읽는 중 도서 목록을 설정한다
        summary.setCurrentReadingList(readingTimerMapper.getReadingBookList(userNumb, Constant.REPORT_STAT_READ));
        // 서울 시간 기준 오늘 완료한 타이머 목록만 설정한다
        summary.setRecentSessionList(readingTimerMapper.getTodayCompletedTimerList(userNumb, Constant.TIMER_STAT_COMPLETED
                                                                                , today.atStartOfDay(), today.plusDays(1L).atStartOfDay()));
        // 조합이 끝난 타이머 화면 요약을 반환한다
        return summary;
    }

    /**
     * 실행 중 측정 구간을 확정하고 날짜별 집계에 저장한다
     *
     * @author SeungHyeon.Kang
     * @param timerDto 실행 중 세션
     * @param now 상태 전환 서버 일시
     */
    private void closeRunningSegment(ReadingTimerDto timerDto, LocalDateTime now) {

        long remainingSeconds = Math.max(0L, properties.getMaxSessionSeconds() - timerDto.getReadSecs());
        LocalDateTime segmentEnd = getSegmentEnd(timerDto.getLastStrt(), now, remainingSeconds);
        long addedSeconds = setDailySegments(timerDto.getUserNumb(), timerDto.getLastStrt(), segmentEnd, now);
        // 확정된 구간만 세션 누적 시간에 더한다
        timerDto.setReadSecs(timerDto.getReadSecs() + addedSeconds);
    }

    /**
     * 실행 세션을 예약된 목표시각까지 확정하고 완료 상태로 저장한다
     *
     * @author SeungHyeon.Kang
     * @param timerDto 목표시간이 지난 실행 세션
     * @param updtDate 완료 처리를 실행한 서버 일시
     */
    private void uptTimerTargetOver(ReadingTimerDto timerDto, LocalDateTime updtDate) {

        // 목표시간 예약 일시를 실제 독서 종료시각으로 사용한다
        LocalDateTime targetEndDate = timerDto.getAlrmDate();
        // 목표 종료시각까지의 실행 구간만 날짜별 독서시간에 확정한다
        closeRunningSegment(timerDto, targetEndDate);
        // 목표시간이 끝난 세션을 완료 상태로 변경한다
        timerDto.setTmrxStat(Constant.TIMER_STAT_COMPLETED);
        // 완료 세션의 최근 실행 시작 시각을 비운다
        timerDto.setLastStrt(null);
        // 목표시간 예약 일시를 세션 완료 일시로 설정한다
        timerDto.setEndxDate(targetEndDate);
        // 알림 실패 시 완료 상태를 유지한 채 재시도할 수 있도록 예약 일시는 보존한다
        timerDto.setAlrmDate(targetEndDate);
        // 실제 자동 완료 처리 시각을 수정 일시로 설정한다
        timerDto.setUpdtDate(updtDate);
        // 확정 독서시간과 완료 상태를 알림 처리보다 먼저 저장한다
        readingTimerMapper.uptTimer(timerDto);
    }

    /**
     * 자정을 넘긴 측정 구간을 날짜별로 나누어 저장한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param segmentStart 측정 구간 시작 일시
     * @param segmentEnd 측정 구간 종료 일시
     * @param updtDate 집계 수정 일시
     * @return 저장한 전체 독서 시간 초
     */
    private long setDailySegments(Long userNumb, LocalDateTime segmentStart, LocalDateTime segmentEnd, LocalDateTime updtDate) {

        long addedSeconds = 0L;
        LocalDateTime cursor = segmentStart;
        // 구간 종료까지 날짜 경계 단위로 시간을 나눈다
        while (cursor.isBefore(segmentEnd)) {
            LocalDateTime nextDay = cursor.toLocalDate().plusDays(1L).atStartOfDay();
            LocalDateTime sliceEnd = segmentEnd.isBefore(nextDay) ? segmentEnd : nextDay;
            long readSeconds = Duration.between(cursor, sliceEnd).getSeconds();
            // 1초 이상인 구간만 일별 집계에 누적한다
            if (readSeconds > 0L) {
                // 해당 날짜에 확정 독서 시간을 누적한다
                readingTimerMapper.setReadingDaily(userNumb, cursor.toLocalDate(), readSeconds, updtDate);
                addedSeconds += readSeconds;
            }
            cursor = sliceEnd;
        }
        // 날짜별로 저장한 전체 독서 시간을 반환한다
        return addedSeconds;
    }

    /**
     * 실행 중 미확정 구간을 주간 응답 계산에만 더한다
     *
     * @author SeungHyeon.Kang
     * @param timerDto 실행 중 세션
     * @param now 응답 기준 서버 일시
     * @param dailySeconds 날짜별 독서 시간 맵
     */
    private void addLiveSegment(ReadingTimerDto timerDto, LocalDateTime now, Map<LocalDate, Long> dailySeconds) {

        long remainingSeconds = Math.max(0L, properties.getMaxSessionSeconds() - timerDto.getReadSecs());
        LocalDateTime segmentEnd = getSegmentEnd(timerDto.getLastStrt(), now, remainingSeconds);
        LocalDateTime cursor = timerDto.getLastStrt();
        // 현재 구간을 날짜별로 나누어 응답용 맵에 더한다
        while (cursor.isBefore(segmentEnd)) {
            LocalDateTime nextDay = cursor.toLocalDate().plusDays(1L).atTime(LocalTime.MIN);
            LocalDateTime sliceEnd = segmentEnd.isBefore(nextDay) ? segmentEnd : nextDay;
            long readSeconds = Duration.between(cursor, sliceEnd).getSeconds();
            // 현재 날짜의 저장 시간에 실행 중 시간을 더한다
            dailySeconds.merge(cursor.toLocalDate(), readSeconds, Long::sum);
            cursor = sliceEnd;
        }
    }

    /**
     * 단일 세션 최대 시간을 넘지 않는 측정 구간 종료 시각을 계산한다
     *
     * @author SeungHyeon.Kang
     * @param segmentStart 측정 구간 시작 일시
     * @param now 현재 서버 일시
     * @param remainingSeconds 세션에 남은 기록 가능 시간 초
     * @return 유효 측정 구간 종료 일시
     */
    private LocalDateTime getSegmentEnd(LocalDateTime segmentStart, LocalDateTime now, long remainingSeconds) {

        // 남은 기록 가능 시간을 적용한 종료 후보를 계산한다
        LocalDateTime cappedEnd = segmentStart.plusSeconds(remainingSeconds);
        // 현재 시각과 최대 시간 후보 중 빠른 시각을 반환한다
        return now.isBefore(cappedEnd) ? now : cappedEnd;
    }

    /**
     * 화면 카운터에 표시할 현재 세션 누적 시간을 계산한다
     *
     * @author SeungHyeon.Kang
     * @param timerDto 실행 중 세션
     * @param now 현재 서버 일시
     * @return 최대 세션 시간을 적용한 누적 독서 시간 초
     */
    private long getLiveTotal(ReadingTimerDto timerDto, LocalDateTime now) {

        long liveSeconds = Math.max(0L, Duration.between(timerDto.getLastStrt(), now).getSeconds());
        // 최대 세션 시간을 넘지 않는 현재 누적 시간을 반환한다
        return Math.min(properties.getMaxSessionSeconds(), timerDto.getReadSecs() + liveSeconds);
    }

    /**
     * 완료되지 않은 사용자 타이머 한 건을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 진행 또는 일시정지 세션
     */
    private ReadingTimerDto getActiveTimer(Long userNumb) {

        // 진행 중과 일시정지 상태에 해당하는 최신 세션을 반환한다
        return readingTimerMapper.getActiveTimerDtl(userNumb, Constant.TIMER_STAT_RUNNING, Constant.TIMER_STAT_PAUSED);
    }

    /**
     * 요청한 타이머 상태가 허용 목록에 포함되는지 확인한다
     *
     * @author SeungHyeon.Kang
     * @param timerStat 검증할 타이머 상태
     * @return 허용된 상태이면 true
     */
    private boolean isTimerStat(String timerStat) {

        // 실행, 일시정지 또는 완료 상태만 허용한다
        return Constant.TIMER_STAT_RUNNING.equals(timerStat)
                || Constant.TIMER_STAT_PAUSED.equals(timerStat)
                || Constant.TIMER_STAT_COMPLETED.equals(timerStat);
    }

    /**
     * 실행 세션이 설정한 목표시간에 도달했는지 확인한다
     *
     * @author SeungHyeon.Kang
     * @param timerDto 확인할 독서 타이머 세션
     * @param now 상태 변경 요청 서버 일시
     * @return 실행 중이며 목표 종료시각에 도달했으면 true
     */
    private boolean isTimerTargetOver(ReadingTimerDto timerDto, LocalDateTime now) {

        // 실행 상태와 아직 발송되지 않은 목표 예약이 모두 있는 세션만 자동 완료한다
        return Constant.TIMER_STAT_RUNNING.equals(timerDto.getTmrxStat())
                && !StringUtil.isEmpty(timerDto.getAlrmDate())
                && !now.isBefore(timerDto.getAlrmDate());
    }

    /**
     * 요청한 알림 목표시간이 단일 세션 허용 범위인지 확인한다
     *
     * @author SeungHyeon.Kang
     * @param targetSeconds 검증할 목표 독서 시간 초
     * @return 1분 이상 최대 세션 시간 이하이면 true
     */
    private boolean isTargetSeconds(long targetSeconds) {

        // 1분 이상이며 단일 세션 최대시간을 넘지 않는 값만 허용한다
        return targetSeconds >= 60L && targetSeconds <= properties.getMaxSessionSeconds();
    }

    /**
     * 일시정지 세션을 재개할 때 아직 읽어야 할 시간으로 알림 예정 일시를 계산한다
     *
     * @author SeungHyeon.Kang
     * @param timerDto 재개할 독서 타이머 세션
     * @param now 재개 서버 일시
     * @return 알림 예정 일시 또는 예약할 알림이 없으면 null
     */
    private LocalDateTime getAlarmDate(ReadingTimerDto timerDto, LocalDateTime now) {

        // 목표시간이 없거나 이미 알림을 발송한 세션은 다시 예약하지 않는다
        if (StringUtil.isEmpty(timerDto.getTargSecs()) || !StringUtil.isEmpty(timerDto.getSendDate())) {
            // 예약할 알림이 없는 상태를 반환한다
            return null;
        }

        long remainingSeconds = Math.max(0L, timerDto.getTargSecs() - timerDto.getReadSecs());
        // 남은 목표시간을 재개 시각에 더한 알림 예정 일시를 반환한다
        return now.plusSeconds(remainingSeconds);
    }

    /**
     * 설정된 서비스 시간대의 현재 서버 일시를 조회한다
     *
     * @author SeungHyeon.Kang
     * @return 현재 서버 일시
     */
    private LocalDateTime getNow() {

        // 주입된 시계를 서비스 시간대로 변환한 현재 일시를 반환한다
        return LocalDateTime.ofInstant(clock.instant(), zoneId);
    }
}
