/**
 * fileName       : usePublicReportPage
 * author         : HanWon.Jang
 * date           : 2026-07-28
 * description    : 공개 독후감 목록 페이지의 조회와 필터 및 사용자 동작을 관리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        HanWon.Jang        최초 생성
 * 2026-08-22        HanWon.Jang        공통 독후감 목록 UI 연계
 */
import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate, useParams, useSearchParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import type { CustomSelectOption } from "@/components/Select/CustomSelect";
import {
  usePublicReportLike,
  usePublicReportsByIsbn,
} from "@/features/Book/Detail/hook/usePublicReports";
import { REPORT_STATUS_CODE_GROUP } from "@/features/Book/constants/reportForm";
import type { PublicReportType } from "@/features/Book/types/book.type";
import type { PublicReportSortType } from "@/features/Book/api/bookApi";
import { getPublicReportTargetApi } from "@/features/Book/api/bookApi";
import type {
  ReportListBookSummary,
  ReportListItem,
} from "@/features/Book/types/reportList.type";
import {
  createReportListItems,
  getReportStatus,
} from "@/features/Book/utils/reportListView";
import { useCodeList } from "@/features/Common/utils/codeUtil";
import { message } from "@/app/messages/message";

export type ReportSort = PublicReportSortType;
export type ReportStatus = string;

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
  // 알림 직접 진입 경로에서 공개 독후감 번호를 조회한다
  const { reptNumb: targetReptNumbParam } = useParams();
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

  const targetReptNumb = Number(targetReptNumbParam);
  const hasTargetReport = Number.isSafeInteger(targetReptNumb) && targetReptNumb > 0;
  const requestedReplyNumb = Number(searchParams.get("replNumb"));
  const focusReplNumb = Number.isSafeInteger(requestedReplyNumb) && requestedReplyNumb > 0
    ? requestedReplyNumb
    : undefined;
  const targetReportQuery = useQuery({
    queryKey: ["publicReportTarget", targetReptNumb],
    queryFn: async () => await getPublicReportTargetApi(targetReptNumb),
    enabled: hasTargetReport,
  });
  const targetReport = targetReportQuery.data?.data;
  const isbn = targetReport?.bookIsbn ?? searchParams.get("isbn") ?? "";
  const isValidIsbn = hasTargetReport || isbn.trim().length > 0;
  // ISBN별 공개 독후감 목록의 서버 상태를 조회한다
  const publicReportsQuery = usePublicReportsByIsbn(
    isbn,
    sort,
    status,
    isbn.trim().length > 0,
  );
  // 공개 독후감 필터와 상태명 표시에 사용할 독서 상태 공통코드를 조회한다
  const reportStatusCodeQuery = useCodeList(REPORT_STATUS_CODE_GROUP);
  // 공개 독후감 좋아요 변경 요청 상태를 조회한다
  const likeMutation = usePublicReportLike();
  const locationPageState = (location.state ?? {}) as ReportListBookSummary;
  const pageState: ReportListBookSummary = targetReport
    ? {
        title: targetReport.bookTitl,
        author: targetReport.bookAthr,
        cover: targetReport.bookCvim,
        ratingAverage: targetReport.bookAvgGrde,
      }
    : locationPageState;

  // 공개 독후감 API 응답이 없을 때도 화면에서 안전하게 빈 목록을 사용한다
  const reports = useMemo(() => {
    // 조회된 공개 독후감 서버 페이지를 화면 정렬 순서대로 연결해 반환한다
    const pageReports = publicReportsQuery.data?.pages.flatMap((page) => page.data?.list ?? []) ?? [];

    if (!targetReport) {
      return pageReports;
    }

    return [targetReport, ...pageReports.filter((report) => report.reptNumb !== targetReport.reptNumb)];
  }, [publicReportsQuery.data, targetReport]);

  // 공개 독후감 알림 직접 진입이면 대상 카드를 표시하고 댓글 목록을 자동으로 연다
  useEffect(() => {
    if (targetReport?.reptNumb) {
      setCommentReport(targetReport);
    }
  }, [targetReport?.reptNumb]);

  // 전체 필터와 서버 공통코드 순서를 결합한 독서 상태 옵션을 생성한다
  const statusOptions = useMemo<
    readonly CustomSelectOption<ReportStatus>[]
  >(() => {
    // 화면 전용 전체 옵션 뒤에 서버가 관리하는 독서 상태 옵션을 반환한다
    return [
      { value: "ALL", label: /* "전체" */ message("frontend.common.all") },
      ...(reportStatusCodeQuery.data ?? [])
        .filter((code) => code.comdCode.toUpperCase() !== "READ")
        .map((code) => ({
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
  const visibleReports = useMemo<ReportListItem[]>(() => {
    const filteredReports =
      status === "ALL"
        ? reports
        : reports.filter((report) => getReportStatus(report) === status);
    // 공통 카드 컴포넌트가 사용할 공개 독후감 표시 모델을 반환한다
    return createReportListItems(filteredReports, expandedReports, statusNameByCode);
  }, [expandedReports, reports, status, statusNameByCode]);

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
    // 독후감 번호를 좋아요 API 요청 대상으로 전달하고 작성자는 서버에서 확정한다
    likeMutation.mutate({
      tagtType: "REPORT",
      tagtNumb: report.reptNumb,
      likeCnt: report.likeCnt,
      likeYsno: report.likeYsno,
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

  /**
   * 공개 독후감 목록의 다음 서버 페이지를 조회한다.
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleLoadMore = (): void => {
    // 목록 하단에 도달하면 현재 공개 조회 조건의 다음 페이지를 요청한다
    void publicReportsQuery.fetchNextPage();
  };

  // 공개 독후감 페이지 UI가 계산 없이 사용할 상태와 이벤트를 반환한다
  return {
    pageState,
    isValidIsbn,
    isPending: hasTargetReport ? targetReportQuery.isPending : publicReportsQuery.isPending,
    isError: hasTargetReport ? targetReportQuery.isError : publicReportsQuery.isError,
    error: hasTargetReport ? targetReportQuery.error : publicReportsQuery.error,
    reportsCount: reports.length,
    visibleReports,
    hasNext: Boolean(publicReportsQuery.hasNextPage),
    isFetchingNext: publicReportsQuery.isFetchingNextPage,
    sort,
    status,
    statusOptions,
    commentReport,
    focusReplNumb,
    isLikePending: likeMutation.isPending,
    handleSortChange,
    handleStatusChange,
    handleToggleReport,
    handleProfileClick,
    handleLike,
    handleOpenReplySheet,
    handleCloseReplySheet,
    handleLoadMore,
  };
}
