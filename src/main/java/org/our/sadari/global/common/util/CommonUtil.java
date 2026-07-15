package org.our.sadari.global.common.util;

import java.time.LocalDate;

/**
 * 공통 유틸리티.
 *
 * @author Seunghyeon.Kang
 */
public class CommonUtil {

    /**
     * 두 날짜 구간이 서로 겹치는지 시작일과 종료일을 포함해 확인한다.
     *
     * @author Seunghyeon.Kang
     * @param startDate 비교할 첫 번째 날짜 구간의 시작일
     * @param endDate 비교할 첫 번째 날짜 구간의 종료일
     * @param targetStartDate 겹침 여부를 확인할 대상 구간의 시작일
     * @param targetEndDate 겹침 여부를 확인할 대상 구간의 종료일
     * @return 두 날짜 구간이 하루라도 겹치면 true, 전혀 겹치지 않으면 false
     */
    public static boolean isDateRangeOverlapped(
            LocalDate startDate,
            LocalDate endDate,
            LocalDate targetStartDate,
            LocalDate targetEndDate
    ) {
        return DateUtil.isDateRangeOverlapped(startDate, endDate, targetStartDate, targetEndDate);
    }
}
