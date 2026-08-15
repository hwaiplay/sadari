/**
 * fileName       : usePublicReportPage
 * author         : HanWon.Jang
 * date           : 2026-07-28
 * description    : 공개 독후감 목록 페이지의 조회와 필터 및 사용자 동작을 관리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        HanWon.Jang        최초 생성
 */
import { useMemo, useState } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import type { CustomSelectOption } from "@/components/Select/CustomSelect";
import {
  usePublicReportLike,
  usePublicReportsByIsbn,
} from "@/features/Book/Detail/hook/usePublicReports";
import { REPORT_STATUS_CODE_GROUP } from "@/features/Book/constants/reportForm";
import type { PublicReportType } from "@/features/Book/types/book.type";
import type { PublicReportSortType } from "@/features/Book/api/bookApi";
import { useCodeList } from "@/features/Common/utils/codeUtil";

const CONTENT_PREVIEW_LENGTH = 180;

export type ReportSort = PublicReportSortType;
export type ReportStatus = string;
export type ReportStatusTone = "done" | "reading" | "stopped";

export type PublicReportPageState = {
  title?: string;
  author?: string;
  cover?: string;
  ratingAverage?: number | string | null;
};

export type PublicReportViewType = PublicReportType & {
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

/**
 * 공개 독후감의 독서 상태 코드를 비교 가능한 대문자로 정규화한다
 *
 * @author HanWon.Jang
 * @param report 독서 상태를 확인할 공개 독후감
 * @return 공백을 제거하고 대문자로 변환한 독서 상태 코드
 */
function getReportStatus(report: PublicReportType): string {
  // 공개 독후감 필터와 상태 이름 조회에 사용할 정규화된 코드를 반환한다
  return String(report.reptStat ?? "")
    .trim()
    .toUpperCase();
}

/**
 * 공개 독후감의 좋아요와 댓글 개수를 화면 표시 문자열로 변환한다
 *
 * @author HanWon.Jang
 * @param countValue 화면에 표시할 개수
 * @return 최대 표시 한도를 적용한 개수 문자열
 */
function getCountLabel(countValue?: number): string {
  const count = Number(countValue) || 0;

  // 네 자리 이상의 개수는 카드 너비를 넘지 않도록 최대 표시 문구를 사용한다
  if (count > 999) {
    // 카드 지표 영역의 최대 개수 문구를 반환한다
    return "999+";
  }

  // 세 자리 이하의 개수를 숫자 문자열로 반환한다
  return String(count);
}

/**
 * 독서 상태 코드에 대응하는 화면 색상 구분값을 결정한다
 *
 * @author HanWon.Jang
 * @param reportStatus 정규화된 독서 상태 코드
 * @return 완료와 중단 및 독서 중 상태를 구분하는 값
 */
function getStatusTone(reportStatus: string): ReportStatusTone {
  // 완독 상태는 완료 전용 색상 구분값을 사용한다
  if (reportStatus === "DONE") {
    // 완독 상태 색상 구분값을 반환한다
    return "done";
  }

  // 독서 중단 상태는 중단 전용 색상 구분값을 사용한다
  if (reportStatus === "STOP") {
    // 독서 중단 상태 색상 구분값을 반환한다
    return "stopped";
  }

  // 나머지 상태는 독서 중 색상 구분값을 반환한다
  return "reading";
}

/**
 * 공개 독후감 목록 페이지의 서버 상태와 필터 및 사용자 동작을 제공한다
 *
 * @author HanWon.Jang
 * @return 공개 독후감 페이지 UI가 사용할 조회 결과와 이벤트 처리 함수
 */
export function usePublicReportPage() {
  // 현재 페이지의 도서 표시 정보를 조회한다
  const location = useLocation();
  // 사용자 프로필 화면 이동에 사용할 라우터 함수를 조회한다
  const navigate = useNavigate();
  // 공개 독후감 조회 대상 ISBN을 URL 검색 조건에서 조회한다
  const [searchParams] = useSearchParams();
  const [expandedReports, setExpandedReports] = useState<Record<number, boolean>>(
    {},
  );
  const [sort, setSort] = useState<ReportSort>("RELATION_DESC");
  const [status, setStatus] = useState<ReportStatus>("ALL");
  const [commentReport, setCommentReport] = useState<PublicReportType | null>(
    null,
  );

  const isbn = searchParams.get("isbn") ?? "";
  const isValidIsbn = isbn.trim().length > 0;
  // ISBN별 공개 독후감 목록의 서버 상태를 조회한다
  const publicReportsQuery = usePublicReportsByIsbn(isbn, sort, isValidIsbn);
  // 공개 독후감 필터와 상태명 표시에 사용할 독서 상태 공통코드를 조회한다
  const reportStatusCodeQuery = useCodeList(REPORT_STATUS_CODE_GROUP);
  // 공개 독후감 좋아요 변경 요청 상태를 조회한다
  const likeMutation = usePublicReportLike();
  const pageState = (location.state ?? {}) as PublicReportPageState;

  // 공개 독후감 API 응답이 없을 때도 화면에서 안전하게 빈 목록을 사용한다
  const reports = useMemo(() => {
    // 공개 독후감 공통 응답의 목록 데이터를 반환한다
    return (publicReportsQuery.data?.data ?? []) as PublicReportType[];
  }, [publicReportsQuery.data]);

  // 전체 필터와 서버 공통코드 순서를 결합한 독서 상태 옵션을 생성한다
  const statusOptions = useMemo<
    readonly CustomSelectOption<ReportStatus>[]
  >(() => {
    // 화면 전용 전체 옵션 뒤에 서버가 관리하는 독서 상태 옵션을 반환한다
    return [
      { value: "ALL", label: "전체" },
      ...(reportStatusCodeQuery.data ?? []).map((code) => ({
        value: code.comdCode,
        label: code.comdName,
      })),
    ];
  }, [reportStatusCodeQuery.data]);

  // 독서 상태 코드별 화면 표시 이름을 반복 탐색하지 않도록 Map으로 변환한다
  const statusNameByCode = useMemo(() => {
    // 대문자 상태 코드를 키로 사용하는 화면 표시 이름 Map을 반환한다
    return new Map(
      (reportStatusCodeQuery.data ?? []).map((code) => [
        code.comdCode.toUpperCase(),
        code.comdName,
      ]),
    );
  }, [reportStatusCodeQuery.data]);

  // 현재 필터와 정렬 및 펼침 상태를 반영한 공개 독후감 화면 모델을 생성한다
  const visibleReports = useMemo<PublicReportViewType[]>(() => {
    const filteredReports =
      status === "ALL"
        ? reports
        : reports.filter((report) => getReportStatus(report) === status);
    // 화면 렌더링에 필요한 파생값을 공개 독후감 데이터와 함께 반환한다
    return filteredReports.map((report) => {
      const rating = Math.max(
        0,
        Math.min(5, Number(report.reptGrde) || 0),
      );
      const reportStatus = getReportStatus(report);
      const reportContent = report.reptCntn ?? "";

      // 공개 독후감 카드가 계산 없이 렌더링할 수 있는 화면 모델을 반환한다
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
        isLongContent: reportContent.length > CONTENT_PREVIEW_LENGTH,
        likeCountLabel: getCountLabel(report.likeCnt),
        commentCountLabel: getCountLabel(report.replCnt),
      };
    });
  }, [expandedReports, reports, sort, status, statusNameByCode]);

  /**
   * 공개 독후감 목록의 정렬 기준을 변경한다
   *
   * @author HanWon.Jang
   * @param nextSort 사용자가 선택한 정렬 기준
   * @return 반환값이 없다
   */
  const handleSortChange = (nextSort: ReportSort): void => {
    // 공개 독후감 화면 모델이 선택한 정렬 기준으로 다시 계산되도록 상태를 변경한다
    setSort(nextSort);
  };

  /**
   * 공개 독후감 목록의 독서 상태 필터를 변경한다
   *
   * @author HanWon.Jang
   * @param nextStatus 사용자가 선택한 독서 상태 코드
   * @return 반환값이 없다
   */
  const handleStatusChange = (nextStatus: ReportStatus): void => {
    // 공개 독후감 화면 모델이 선택한 독서 상태만 포함하도록 필터 상태를 변경한다
    setStatus(nextStatus);
  };

  /**
   * 공개 독후감 본문의 펼침 상태를 반전한다
   *
   * @author HanWon.Jang
   * @param reptNumb 펼침 상태를 변경할 독후감 번호
   * @return 반환값이 없다
   */
  const handleToggleReport = (reptNumb: number): void => {
    // 다른 카드의 펼침 상태를 유지하면서 선택한 카드만 반전한다
    setExpandedReports((previous) => ({
      ...previous,
      [reptNumb]: !previous[reptNumb],
    }));
  };

  /**
   * 공개 독후감 작성자의 소셜 프로필 화면으로 이동한다
   *
   * @author HanWon.Jang
   * @param userNumb 이동할 작성자 사용자 번호
   * @return 반환값이 없다
   */
  const handleProfileClick = (userNumb: number): void => {
    // 잘못된 프로필 경로 이동을 차단하기 위해 유효한 사용자 번호만 허용한다
    if (userNumb > 0) {
      // 작성자 사용자 번호를 포함한 소셜 프로필 경로로 이동한다
      navigate(`/social/profile/${userNumb}`);
    }
  };

  /**
   * 선택한 공개 독후감의 좋아요 상태를 변경한다
   *
   * @author HanWon.Jang
   * @param report 좋아요 대상을 식별할 공개 독후감
   * @return 반환값이 없다
   */
  const handleLike = (report: PublicReportType): void => {
    // 독후감과 작성자 번호를 좋아요 API 요청 대상으로 전달한다
    likeMutation.mutate({
      tagtType: "REPORT",
      tagtNumb: report.reptNumb,
      targetUserNumb: report.userNumb,
    });
  };

  /**
   * 선택한 공개 독후감의 댓글 바텀시트를 연다
   *
   * @author HanWon.Jang
   * @param report 댓글을 조회할 공개 독후감
   * @return 반환값이 없다
   */
  const handleOpenReplySheet = (report: PublicReportType): void => {
    // 댓글 조회 대상과 바텀시트 제목에 사용할 독후감 정보를 저장한다
    setCommentReport(report);
  };

  /**
   * 현재 열린 댓글 바텀시트를 닫는다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleCloseReplySheet = (): void => {
    // 댓글 조회 대상을 제거하여 바텀시트 렌더링을 종료한다
    setCommentReport(null);
  };

  // 공개 독후감 페이지 UI가 계산 없이 사용할 상태와 이벤트를 반환한다
  return {
    pageState,
    isValidIsbn,
    isPending: publicReportsQuery.isPending,
    isError: publicReportsQuery.isError,
    error: publicReportsQuery.error,
    reportsCount: reports.length,
    visibleReports,
    sort,
    status,
    statusOptions,
    commentReport,
    isLikePending: likeMutation.isPending,
    handleSortChange,
    handleStatusChange,
    handleToggleReport,
    handleProfileClick,
    handleLike,
    handleOpenReplySheet,
    handleCloseReplySheet,
  };
}
