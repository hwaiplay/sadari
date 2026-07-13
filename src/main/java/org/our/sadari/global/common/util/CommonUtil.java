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
     * 두 날짜 구간이 서로 겹치는지 시작일과 종료일을 포함해 확인한다.
     * 독서 캘린더처럼 조회 범위와 데이터 보유 기간이 하나라도 겹치면 노출해야 하는 화면에서 사용한다.
     * 종료일이 조회 시작일보다 이전이거나 시작일이 조회 종료일보다 이후인 경우만 겹치지 않는 것으로 판단한다.
     * @Author Seunghyeon.Kang
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
        return !endDate.isBefore(targetStartDate) && !startDate.isAfter(targetEndDate);
    }
}
