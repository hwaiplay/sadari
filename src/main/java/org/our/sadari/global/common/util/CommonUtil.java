package org.our.sadari.global.common.util;

import java.time.LocalDate;

/**
 * fileName       : CommonUtil
 * author         : SeungHyeon.Kang
 * date           : 2026-03-21
 * description    : 공통 처리에 필요한 변환과 판정 기능을 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-21        SeungHyeon.Kang    최초 생성
 */
public class CommonUtil {
    /**
     * 두 날짜 기간의 겹침 여부 판정한다.
     *
     * @author SeungHyeon.Kang
     * @param startDate 기간 계산의 시작일
     * @param endDate 기간 계산의 종료일
     * @param targetStartDate 비교 대상 기간의 시작일
     * @param targetEndDate 비교 대상 기간의 종료일
     * @return 처리 결과
     */
    public static boolean isDateRangeOverlapped(LocalDate startDate, LocalDate endDate, LocalDate targetStartDate
                                              , LocalDate targetEndDate) {
        // 두 날짜 기간의 겹침 여부 판정 결과를 반환한다
        return DateUtil.isDateRangeOverlapped(startDate, endDate, targetStartDate, targetEndDate);
    }
}
