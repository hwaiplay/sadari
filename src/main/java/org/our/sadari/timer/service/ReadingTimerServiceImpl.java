package org.our.sadari.timer.service;

import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.timer.config.ReadingTimerProperties;
import org.our.sadari.timer.dto.ReadingTimerDto;
import org.our.sadari.timer.mapper.ReadingTimerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

/**
 * fileName       : ReadingTimerServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-08-14
 * description    : 서버 시간으로 독서 세션을 확정하고 주간 출석을 계산한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        SeungHyeon.Kang    최초 생성
 * 2026-08-14        SeungHyeon.Kang    오늘 완료 타이머 조회 적용
 */
@Service
@Transactional(readOnly = true)
public class ReadingTimerServiceImpl implements ReadingTimerService {

    // 독서 타이머 데이터 접근 객체
    private final ReadingTimerMapper readingTimerMapper;
    // 독서 타이머 운영 기준
    private final ReadingTimerProperties properties;
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
     */
    @Autowired
    public ReadingTimerServiceImpl(ReadingTimerMapper readingTimerMapper, ReadingTimerProperties properties) {

        this(readingTimerMapper, properties, Clock.system(ZoneId.of(properties.getZoneId())));
    }

    /**
     * 테스트에서 고정 시계를 사용할 수 있도록 독서 타이머 서비스를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param readingTimerMapper 독서 타이머 데이터 접근 객체
     * @param properties 독서 타이머 운영 기준
     * @param clock 현재 일시 제공 시계
     */
    ReadingTimerServiceImpl(ReadingTimerMapper readingTimerMapper, ReadingTimerProperties properties, Clock clock) {

        this.readingTimerMapper = readingTimerMapper;
        this.properties = properties;
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

        // 새 세션에 동일한 서버 시작 시각을 적용한다
        LocalDateTime now = getNow();
        ReadingTimerDto timerDto = new ReadingTimerDto();
        // 로그인 사용자 번호를 새 세션에 설정한다
        timerDto.setUserNumb(userNumb);
        // 요청한 독후감 번호를 새 세션에 설정한다
        timerDto.setReptNumb(StringUtil.isEmpty(request) ? null : request.getReptNumb());
        // 새 세션을 실행 중 상태로 설정한다
        timerDto.setTmrxStat(Constant.TIMER_STAT_RUNNING);
        // 세션 최초 시작 일시를 설정한다
        timerDto.setStrtDate(now);
        // 현재 측정 구간 시작 일시를 설정한다
        timerDto.setLastStrt(now);
        // 확정 독서 시간을 0초로 설정한다
        timerDto.setReadSecs(Constant.NUMBER_ZERO);
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

        // 상태 전환 시점의 서버 시간을 고정한다
        LocalDateTime now = getNow();
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
        } else {
            // 측정하지 않는 상태에서는 최근 시작 시각을 비운다
            timerDto.setLastStrt(null);
            // 완료 상태일 때만 완료 일시를 기록한다
            timerDto.setEndxDate(Constant.TIMER_STAT_COMPLETED.equals(targetStat) ? now : null);
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
        ReadingTimerDto timerDto = getActiveTimer(userNumb);
        // 진행 중인 세션이 없다면 별도 변경 없이 종료한다
        if (StringUtil.isEmpty(timerDto)) {
            // 완료할 세션이 없는 정상 흐름을 반환한다
            return;
        }
        // 계정 처리 시점까지 실행 중인 독서 시간을 확정한다
        LocalDateTime now = getNow();
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
        // 수정 일시를 설정한다
        timerDto.setUpdtDate(now);
        // 완료된 세션 값을 저장한다
        readingTimerMapper.uptTimer(timerDto);
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
