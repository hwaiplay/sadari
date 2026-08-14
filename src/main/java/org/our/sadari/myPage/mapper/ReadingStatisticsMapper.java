package org.our.sadari.myPage.mapper;

import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.myPage.dto.ReadingStatisticsDto;
import org.our.sadari.myPage.dto.ReadingStatisticsQueryDto;
import org.our.sadari.myPage.dto.ReadingStatisticsSettingDto;

/**
 * fileName       : ReadingStatisticsMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-08-14
 * description    : 본인과 공개 프로필의 독서 시간과 습관 및 독후감 통계 SQL 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        SeungHyeon.Kang    최초 생성
 * 2026-08-14        SeungHyeon.Kang    연속 기록과 책별 시간 및 별점과 연도 비교 조회 추가
 * 2026-08-14        SeungHyeon.Kang    별점을 소수점 버림한 1점 단위로 집계
 * 2026-08-14        SeungHyeon.Kang    올해 상위 도서의 독후감 이동 번호 조회
 */
@Mapper
public interface ReadingStatisticsMapper {

    /**
     * 회원의 독서 통계 공개 범위 및 계정 상태를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 설정을 조회할 회원 번호
     * @param privateYsno 설정 행이 없을 때 적용할 비공개 여부 코드
     * @return 회원의 독서 통계 설정과 계정 상태
     */
    ReadingStatisticsSettingDto getReadingStatsSetting(@Param("userNumb") Long userNumb
                                                       , @Param("privateYsno") String privateYsno);

    /**
     * 회원의 독서 시간 기록이 존재하는 연도를 최근 연도부터 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 연도 목록을 조회할 회원 번호
     * @return 독서 시간 기록이 존재하는 연도 목록
     */
    List<Integer> getReadingYearList(@Param("userNumb") Long userNumb);

    /**
     * 선택 연도의 잔디 구성에 필요한 일별 독서 시간을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param periodStart 조회 시작일
     * @param periodEnd 조회 종료일
     * @return 저장된 날짜별 독서 시간 목록
     */
    List<ReadingStatisticsDto.Daily> getDailyTimeList(@Param("userNumb") Long userNumb
                                                     , @Param("periodStart") LocalDate periodStart
                                                     , @Param("periodEnd") LocalDate periodEnd);

    /**
     * 로그인 사용자의 읽는 중, 완독, 중단 독후감 수를 상태별로 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param readStat 읽는 중 상태 코드
     * @param doneStat 완독 상태 코드
     * @param stopStat 중단 상태 코드
     * @return 독서 상태별 독후감 수 목록
     */
    List<ReadingStatisticsDto.Status> getStatusCountList(@Param("userNumb") Long userNumb
                                                        , @Param("readStat") String readStat
                                                        , @Param("doneStat") String doneStat
                                                        , @Param("stopStat") String stopStat);

    /**
     * 회원이 타이머로 1초 이상 독서한 날짜를 오래된 순서로 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 연속 독서일을 조회할 회원 번호
     * @return 확정 독서 시간이 존재하는 전체 날짜 목록
     */
    List<LocalDate> getReadingDateList(@Param("userNumb") Long userNumb);

    /**
     * 현재 연도에 완료한 타이머 시간을 도서별로 합산해 상위 세 권을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param query 회원과 현재 연도 타이머 기간 및 완료 상태 조건
     * @return 독후감 이동 번호가 포함된 현재 연도 누적 독서 시간이 긴 도서 세 권
     */
    List<ReadingStatisticsDto.BookTime> getTopBookTimeList(ReadingStatisticsQueryDto query);

    /**
     * 회원이 독후감에 저장한 유효한 양수 별점을 소수점 버림한 정수 점수별로 집계한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 별점 분포를 조회할 회원 번호
     * @return 0점부터 5점까지의 정수 별점별 독후감 수 목록
     */
    List<ReadingStatisticsDto.Rating> getRatingCountList(@Param("userNumb") Long userNumb);

    /**
     * 현재 연도와 전년도 같은 기간의 독서 시간과 독서일 및 완독 권수를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param query 회원과 현재 및 이전 연도의 같은 기간 및 완독 상태 조건
     * @return 두 연도의 독서 기록 비교값
     */
    ReadingStatisticsDto.YearComparison getYearComparison(ReadingStatisticsQueryDto query);

    /**
     * 정상 이용 회원의 범용 설정 행에 독서 통계 공개 범위를 저장한다
     *
     * @author SeungHyeon.Kang
     * @param setting 변경할 회원 번호와 독서 통계 공개 설정
     */
    void uptReadingStatsSetting(ReadingStatisticsSettingDto setting);
}
