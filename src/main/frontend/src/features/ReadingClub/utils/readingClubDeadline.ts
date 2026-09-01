import { message } from "@/app/messages/message";
import { getRemainDaysUntil } from "@/app/utils/dateUtil";

export interface ReadingDeadline {
  label: string;
  state: "ENDED" | "TODAY" | "UPCOMING";
}

/**
 * 모임 독서 종료일을 종료, 오늘 또는 D-day 상태로 변환
 *
 * @author Hanwon.Jang
 * @param endValue 모임 독서 목표 종료 일시
 * @return 종료일 문구와 상태 또는 종료일이 없을 때 null
 */
export function getReadingDeadline(endValue?: string): ReadingDeadline | null {
  // 종료일이 없는 모임은 독서 기간 상태를 표시하지 않음
  if (!endValue) {
    // 종료일 상태 문구가 없음을 반환
    return null;
  }

  // API 일시값에서 로컬 날짜 부분만 사용해 남은 일수를 계산
  const remainingDays = getRemainDaysUntil(endValue.slice(0, 10));

  // 종료일이 지난 독서는 완료된 기간으로 표시
  if (remainingDays < 0) {
    // "종료"
    const endedLabel = message("frontend.readingClub.common.deadline.ended");
    // 종료 전용 스타일을 적용할 수 있는 완료 상태와 문구를 반환
    return { label: endedLabel, state: "ENDED" };
  }

  // 종료일 당일은 남은 일수 대신 오늘로 표시
  if (remainingDays === 0) {
    // "오늘"
    const todayLabel = message("frontend.readingClub.common.deadline.today");
    // 종료일 당일의 상태와 문구를 반환
    return { label: todayLabel, state: "TODAY" };
  }

  // "D-{남은 일수}"
  const dDayLabel = message("frontend.readingClub.common.deadline.dDay", [remainingDays]);
  // 종료일까지 하루 이상 남은 독서의 예정 상태와 문구를 반환
  return { label: dDayLabel, state: "UPCOMING" };
}
