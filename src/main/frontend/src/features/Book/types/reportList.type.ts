/**
 * 독후감 목록 공통 UI가 사용하는 도서 요약과 카드 표시 모델을 정의함
 *
 * @author HanWon.Jang
 */
import type { PublicReportType } from "./book.type";

export type ReportStatusTone = "done" | "reading" | "stopped";

export type ReportListBookSummary = {
  title?: string;
  author?: string;
  cover?: string;
  ratingAverage?: number | string | null;
};

export type ReportListItem = PublicReportType & {
  rating: number;
  reportStatus: string;
  reportStatusName: string;
  statusTone: ReportStatusTone;
  isExpanded: boolean;
  reportContent: string;
  isLongContent: boolean;
  likeCountLabel: string;
  commentCountLabel: string;
};
