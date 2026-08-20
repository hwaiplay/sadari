package org.our.sadari.timer.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.timer.dto.ReadingTimerDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * fileName       : ReadingTimerMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-08-14
 * description    : 독서 타이머 세션과 일별 집계 SQL 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        SeungHyeon.Kang    최초 생성 및 완료 타이머 조회
 * 2026-08-20        SeungHyeon.Kang    목표시간 알림·도서별 누적 페이지 조회 추가
 */
@Mapper
public interface ReadingTimerMapper {

    /**
     * 동시 시작 요청을 직렬화하도록 사용자 행을 잠근다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 잠근 사용자 번호
     */
    Long getUserLock(@Param("userNumb") Long userNumb);
    /**
     * 타이머에 연결할 독후감이 사용자의 읽는 중 독후감인지 확인한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param reptNumb 연결할 독후감 번호
     * @param readStat 읽는 중 독후감 상태
     * @return 연결 가능한 독후감 건수
     */
    int getReadingReportCnt(@Param("userNumb") Long userNumb, @Param("reptNumb") Long reptNumb, @Param("readStat") String readStat);
    /**
     * 사용자의 완료되지 않은 최근 타이머 세션을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param runningStat 실행 중 상태
     * @param pausedStat 일시정지 상태
     * @return 완료되지 않은 타이머 세션
     */
    ReadingTimerDto getActiveTimerDtl(@Param("userNumb") Long userNumb, @Param("runningStat") String runningStat, @Param("pausedStat") String pausedStat);
    /**
     * 사용자 소유 타이머 세션 한 건을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param tmrxNumb 타이머 세션 번호
     * @return 사용자 소유 타이머 세션
     */
    ReadingTimerDto getTimerDtl(@Param("userNumb") Long userNumb, @Param("tmrxNumb") Long tmrxNumb);
    /**
     * 타이머에 연결할 수 있는 사용자의 읽는 중 도서를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param readStat 읽는 중 독후감 상태
     * @return 연결 가능한 도서 목록
     */
    List<ReadingTimerDto> getReadingBookList(@Param("userNumb") Long userNumb, @Param("readStat") String readStat);
    /**
     * 사용자가 오늘 완료한 타이머를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param completedStat 완료 상태
     * @param todayStart 오늘 시작 일시
     * @param tomorrowStart 내일 시작 일시
     * @return 오늘 완료한 타이머 목록
     */
    List<ReadingTimerDto> getTodayCompletedTimerList(@Param("userNumb") Long userNumb, @Param("completedStat") String completedStat
                                                   , @Param("todayStart") LocalDateTime todayStart, @Param("tomorrowStart") LocalDateTime tomorrowStart);
    /**
     * 완료 타이머 시간을 도서별로 합산해 최근 기록순으로 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param completedStat 완료 상태
     * @param pageOffset 현재 페이지 시작 위치
     * @param pageLimit 다음 페이지 판정용 제한 건수
     * @return 최근 완료 기록순 도서별 누적 독서 시간 페이지 후보 목록
     */
    List<ReadingTimerDto.BookTime> getBookTimeList(@Param("userNumb") Long userNumb
                                                 , @Param("completedStat") String completedStat
                                                 , @Param("pageOffset") int pageOffset
                                                 , @Param("pageLimit") int pageLimit);
    /**
     * 지정한 주간 범위의 일별 확정 독서 시간을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param weekStart 주간 시작일
     * @param weekEnd 주간 종료일
     * @return 날짜별 독서 시간 목록
     */
    List<ReadingTimerDto.Daily> getDailyList(@Param("userNumb") Long userNumb, @Param("weekStart") LocalDate weekStart, @Param("weekEnd") LocalDate weekEnd);
    /**
     * 새 독서 타이머 세션을 등록한다
     *
     * @author SeungHyeon.Kang
     * @param timerDto 등록할 타이머 세션
     * @return 등록 건수
     */
    int setTimer(ReadingTimerDto timerDto);
    /**
     * 타이머 상태와 확정 독서 시간을 수정한다
     *
     * @author SeungHyeon.Kang
     * @param timerDto 수정할 타이머 세션
     * @return 수정 건수
     */
    int uptTimer(ReadingTimerDto timerDto);
    /**
     * 목표시간이 지나 알림 발송을 기다리는 실행 세션 번호를 제한 조회한다
     *
     * @author SeungHyeon.Kang
     * @param runningStat 실행 중 상태
     * @param activeUserStat 정상 이용 회원 상태
     * @param alarmDate 알림 발송 기준 일시
     * @param maxSize 한 번에 조회할 최대 건수
     * @return 발송 대상 타이머 세션 번호 목록
     */
    List<Long> getDueTimerAlimList(@Param("runningStat") String runningStat, @Param("activeUserStat") String activeUserStat
                                  , @Param("alarmDate") LocalDateTime alarmDate, @Param("maxSize") int maxSize);
    /**
     * 발송 대상 여부를 다시 확인하고 동시 발송을 막도록 타이머 세션을 잠근다
     *
     * @author SeungHyeon.Kang
     * @param tmrxNumb 독서 타이머 세션 번호
     * @param runningStat 실행 중 상태
     * @param activeUserStat 정상 이용 회원 상태
     * @param alarmDate 알림 발송 기준 일시
     * @return 발송 대상 독서 타이머 세션
     */
    ReadingTimerDto getDueTimerAlimDtl(@Param("tmrxNumb") Long tmrxNumb, @Param("runningStat") String runningStat
                                      , @Param("activeUserStat") String activeUserStat, @Param("alarmDate") LocalDateTime alarmDate);
    /**
     * 목표시간 알림 발송 완료 일시를 저장하고 예약을 해제한다
     *
     * @author SeungHyeon.Kang
     * @param timerDto 발송 완료 타이머 세션
     * @return 수정 건수
     */
    int uptTimerAlimSent(ReadingTimerDto timerDto);
    /**
     * 사용자와 날짜별 확정 독서 시간을 누적한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param readDate 독서 집계일
     * @param readSecs 누적할 독서 시간 초
     * @param updtDate 집계 수정 일시
     * @return 등록 또는 수정 건수
     */
    int setReadingDaily(@Param("userNumb") Long userNumb, @Param("readDate") LocalDate readDate, @Param("readSecs") long readSecs, @Param("updtDate") LocalDateTime updtDate);
    /**
     * 보존기간이 지난 완료 타이머 세션 상세를 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param completedStat 완료 상태
     * @param expireDate 상세 보존 만료 기준 일시
     * @return 삭제 건수
     */
    int delExpiredTimer(@Param("completedStat") String completedStat, @Param("expireDate") LocalDateTime expireDate);
}
