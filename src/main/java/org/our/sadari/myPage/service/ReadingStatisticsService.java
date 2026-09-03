package org.our.sadari.myPage.service;

import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.myPage.dto.ReadingStatisticsSettingDto;

/**
 * fileName       : ReadingStatisticsService
 * author         : SeungHyeon.Kang
 * date           : 2026-08-14
 * description    : 본인과 공개 프로필의 독서 시간과 습관 및 독후감 통계 조회와 설정 업무를 정의함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        SeungHyeon.Kang    최초 생성 및 독서 통계 계약
 */
public interface ReadingStatisticsService {

    /**
     * 선택 연도의 독서 시간 잔디만 타이머 화면용으로 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param readYear 조회할 연도, 없으면 현재 연도
     * @return 조회 가능한 연도와 날짜별 독서 시간 잔디
     */
    ResultData getReadingHeatmap(Long userNumb, Integer readYear);

    /**
     * 선택 연도 잔디와 독서 습관 및 독후감 통계를 마이페이지 그래프용으로 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param readYear 조회할 연도, 없으면 현재 연도
     * @return 잔디와 연속 기록 및 책별 시간과 상태 및 별점과 연도 비교 통계
     */
    ResultData getReadingStats(Long userNumb, Integer readYear);

    /**
     * 정상 이용 회원이 공개한 독서 통계를 다른 사용자 프로필에 제공함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 공개 통계를 조회할 프로필 회원 번호
     * @param readYear 조회할 연도, 없으면 현재 연도
     * @return 공개 허용 시 독서 통계, 비공개 또는 제한 계정이면 빈 데이터
     */
    ResultData getPublicReadingStats(Long userNumb, Integer readYear);

    /**
     * 로그인 회원의 독서 통계 공개 범위를 변경함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 설정을 변경할 로그인 회원 번호
     * @param setting 선택한 공개 여부
     * @return 저장된 공개 여부 코드
     */
    ResultData uptReadingStatsSetting(Long userNumb, ReadingStatisticsSettingDto setting);
}
