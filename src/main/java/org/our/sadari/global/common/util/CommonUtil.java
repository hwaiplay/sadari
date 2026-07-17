package org.our.sadari.global.common.util;

import java.time.LocalDate;

/**
 * CommonUtil 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
public class CommonUtil {

    /**
     * isDateRangeOverlapped 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param startDate 처리에 필요한 입력값
     * @param endDate 처리에 필요한 입력값
     * @param targetStartDate 처리에 필요한 입력값
     * @param targetEndDate 처리에 필요한 입력값
     * @return 처리 결과
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
