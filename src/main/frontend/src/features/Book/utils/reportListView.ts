/**
 * 독후감 목록 API 데이터를 공통 카드 표시 모델로 변환함
 *
 * @author HanWon.Jang
 */
import type { PublicReportType } from "@/features/Book/types/book.type";
import type {
  ReportListItem,
  ReportStatusTone,
} from "@/features/Book/types/reportList.type";

/** 독후감 목록 카드가 접힌 상태로 표시할 본문 길이 기준임 */
export const REPORT_CONTENT_PREVIEW_LENGTH = 180;

/**
 * 독후감의 독서 상태 코드를 비교 가능한 대문자로 정규화함
 *
 * @author HanWon.Jang
 * @param report 독서 상태를 확인할 독후감
 * @return 공백을 제거하고 대문자로 변환한 독서 상태 코드
 */
export function getReportStatus(report: PublicReportType): string {
  // 목록 필터와 상태 이름 조회에 사용할 정규화된 코드를 반환함
  return String(report.reptStat ?? "")
    .trim()
    .toUpperCase();
}

/**
 * 독후감의 좋아요와 댓글 개수를 화면 표시 문자열로 변환함
 *
 * @author HanWon.Jang
 * @param countValue 화면에 표시할 개수
 * @return 최대 표시 한도를 적용한 개수 문자열
 */
function getCountLabel(countValue?: number): string {
  const count = Number(countValue) || 0;

  // 네 자리 이상의 개수는 카드 너비를 넘지 않도록 최대 표시 문구를 사용함
  if (count > 999) {
    // 카드 지표 영역의 최대 개수 문구를 반환함
    return "999+";
  }

  // 세 자리 이하의 개수를 숫자 문자열로 반환함
  return String(count);
}

/**
 * 독서 상태 코드에 대응하는 카드 색상 구분값을 결정함
 *
 * @author HanWon.Jang
 * @param reportStatus 정규화된 독서 상태 코드
 * @return 완료와 중단 및 독서 중 상태를 구분하는 값
 */
function getStatusTone(reportStatus: string): ReportStatusTone {
  // 완독 상태는 완료 전용 색상 구분값을 사용함
  if (reportStatus === "DONE") {
    // 완독 상태 색상 구분값을 반환함
    return "done";
  }

  // 독서 중단 상태는 중단 전용 색상 구분값을 사용함
  if (reportStatus === "STOP") {
    // 독서 중단 상태 색상 구분값을 반환함
    return "stopped";
  }

  // 나머지 상태는 독서 중 색상 구분값을 반환함
  return "reading";
}

/**
 * 독후감 목록을 카드가 계산 없이 렌더링할 수 있는 표시 모델로 변환함
 *
 * @author HanWon.Jang
 * @param reports 화면에 표시할 독후감 목록
 * @param expandedReports 독후감 번호별 본문 펼침 상태
 * @param statusNameByCode 상태 코드별 화면 표시 이름
 * @return 카드 표시용 독후감 목록
 */
export function createReportListItems(
  reports: PublicReportType[],
  expandedReports: Record<number, boolean>,
  statusNameByCode: ReadonlyMap<string, string>,
): ReportListItem[] {
  // 각 독후감에 별점과 상태 및 개수 표시값을 결합해 반환함
  return reports.map((report) => {
    const rating = Math.max(0, Math.min(5, Number(report.reptGrde) || 0));
    const reportStatus = getReportStatus(report);
    const reportContent = report.reptCntn?.trim() ?? "";

    // 공통 독후감 카드가 사용할 계산 결과를 원본 데이터와 함께 반환함
    return {
      ...report,
      rating,
      reportStatus,
      reportStatusName:
        report.reptStatName
        || statusNameByCode.get(reportStatus)
        || reportStatus,
      statusTone: getStatusTone(reportStatus),
      isExpanded: Boolean(expandedReports[report.reptNumb]),
      reportContent,
      isLongContent: reportContent.length > REPORT_CONTENT_PREVIEW_LENGTH,
      likeCountLabel: getCountLabel(report.likeCnt),
      commentCountLabel: getCountLabel(report.replCnt),
    };
  });
}
