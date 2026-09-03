package org.our.sadari.myPage.mapper;

import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.myPage.dto.ReadingHeatmapRowDto;
import org.our.sadari.myPage.dto.ReadingStatisticsAggregateDto;
import org.our.sadari.myPage.dto.ReadingStatisticsDto;
import org.our.sadari.myPage.dto.ReadingStatisticsQueryDto;
import org.our.sadari.myPage.dto.ReadingStatisticsSettingDto;

/**
 * fileName       : ReadingStatisticsMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-08-14
 * description    : 본인과 공개 프로필의 독서 시간과 습관 및 독후감 통계 SQL 계약을 정의함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        SeungHyeon.Kang    최초 생성 및 독서 통계 조회
 * 2026-08-15        SeungHyeon.Kang    잔디와 통계 집계 SQL 왕복 통합
 */
@Mapper
public interface ReadingStatisticsMapper {

    /**
     * 회원의 독서 통계 공개 범위 및 계정 상태를 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 설정을 조회할 회원 번호
     * @param privateYsno 설정 행이 없을 때 적용할 비공개 여부 코드
     * @return 회원의 독서 통계 설정과 계정 상태
     */
    ReadingStatisticsSettingDto getReadingStatsSetting(@Param("userNumb") Long userNumb
                                                       , @Param("privateYsno") String privateYsno);

    /**
     * 조회 가능한 기록 연도와 선택 연도의 일별 독서 시간을 한 번에 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 잔디를 조회할 회원 번호
     * @param periodStart 선택 연도 시작일
     * @param periodEnd 선택 연도 종료일
     * @return 연도 행과 선택 기간의 일별 독서 시간 행
     */
    List<ReadingHeatmapRowDto> getHeatmapRowList(@Param("userNumb") Long userNumb
                                                , @Param("periodStart") LocalDate periodStart
                                                , @Param("periodEnd") LocalDate periodEnd);

    /**
     * 현재 연도에 완료한 타이머 시간을 도서별로 합산해 상위 세 권을 조회함
     *
     * @author SeungHyeon.Kang
     * @param query 회원과 현재 연도 타이머 기간 및 완료 상태 조건
     * @return 독후감 이동 번호가 포함된 현재 연도 누적 독서 시간이 긴 도서 세 권
     */
    List<ReadingStatisticsDto.BookTime> getTopBookTimeList(ReadingStatisticsQueryDto query);

    /**
     * 독서 상태와 연속 기록 및 별점과 연도 비교값을 한 번에 집계함
     *
     * @author SeungHyeon.Kang
     * @param query 회원과 현재 및 이전 연도 비교 기간 및 상태 코드
     * @return 화면 목록으로 변환할 통합 집계값
     */
    ReadingStatisticsAggregateDto getStatsAggregate(ReadingStatisticsQueryDto query);

    /**
     * 정상 이용 회원의 범용 설정 행에 독서 통계 공개 범위를 저장함
     *
     * @author SeungHyeon.Kang
     * @param setting 변경할 회원 번호와 독서 통계 공개 설정
     */
    void uptReadingStatsSetting(ReadingStatisticsSettingDto setting);
}
