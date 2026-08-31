/**
 * 잠긴 시작일을 유지하면서 선택한 날짜를 종료일로 사용할 수 있는지 확인한다
 *
 * @author HanWon.Jang
 * @param startDate 잠긴 독서 시작일
 * @param selectedDate 달력에서 선택한 종료일
 * @return 유효한 날짜 범위 또는 시작일보다 이른 경우 null
 */
export const getLockedDateRange = (startDate: string, selectedDate: string) => {

  return selectedDate < startDate ? null : [startDate, selectedDate] as const;
};
