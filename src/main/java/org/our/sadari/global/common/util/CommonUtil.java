package org.our.sadari.global.common.util;

import java.time.LocalDate;

/**
 * fileName       : SeungHyeon.Kang
 * author         : USER
 * date           : 2026-03-21
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-21        USER       최초 생성
 */
public class CommonUtil {

    /**
     * Two date ranges are checked for overlap including both range boundaries.
     * @Author Seunghyeon.Kang
     * @param startDate Start date of the first range.
     * @param endDate End date of the first range.
     * @param targetStartDate Start date of the target range.
     * @param targetEndDate End date of the target range.
     * @return Whether the two ranges overlap.
     */
    public static boolean isDateRangeOverlapped(
            LocalDate startDate,
            LocalDate endDate,
            LocalDate targetStartDate,
            LocalDate targetEndDate
    ) {
        return !endDate.isBefore(targetStartDate) && !startDate.isAfter(targetEndDate);
    }
}
