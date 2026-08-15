import { formatDashedDateToDot } from "@/app/utils/dateUtil";
import type { ReadingSummaryReport } from "@/features/User/api/userApi";

/**
 * 프로필 독후감 요약의 종료일을 화면 날짜 형식으로 변환한다
 *
 * @author SeungHyeon.Kang
 * @param report 종료일을 표시할 독후감 요약
 * @return 점으로 구분한 독서 종료일 문자열
 */
export function getReadingEndDateText(report: ReadingSummaryReport): string {
  // 두 프로필 화면이 같은 날짜 보정 규칙을 사용하도록 공통 포맷 결과를 반환한다
  return formatDashedDateToDot(report.reptEndt);
}

/**
 * 프로필 독후감 평점을 다섯 개 기준의 별 문자열로 변환한다
 *
 * @author SeungHyeon.Kang
 * @param grade 서버에서 내려온 평점 문자열
 * @return 채운 별과 빈 별을 합친 다섯 개 별점 문자열
 */
export function getReadingGradeText(grade?: string): string {
  // 잘못된 평점도 화면이 깨지지 않도록 0점부터 5점 범위의 정수로 보정한다
  const gradeNumber = Math.max(0, Math.min(5, Math.floor(Number(grade) || 0)));
  // 보정된 평점 수만큼 채운 별과 남은 빈 별을 연결해 반환한다
  return `${"\u2605".repeat(gradeNumber)}${"\u2606".repeat(5 - gradeNumber)}`;
}
