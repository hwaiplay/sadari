/**
 * 완료된 모임 독서 회차의 DONE 독후감 목록 페이지 상태를 관리한다.
 *
 * @author HanWon.Jang
 */
import { useMemo, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { usePublicReportLike } from "@/features/Book/Detail/hook/usePublicReports";
import type { PublicReportSortType } from "@/features/Book/api/bookApi";
import type { PublicReportType } from "@/features/Book/types/book.type";
import type { ReportListBookSummary } from "@/features/Book/types/reportList.type";
import { createReportListItems } from "@/features/Book/utils/reportListView";
import { useReadingRoundReports } from "@/features/ReadingClub/hooks/useReadingRoundReports";

const EMPTY_STATUS_NAMES = new Map<string, string>();

/**
 * 모임 회차 독후감 페이지의 전용 조회 상태와 사용자 동작을 제공한다.
 *
 * @author HanWon.Jang
 * @return 완료 회차 DONE 독후감 페이지 상태와 처리 함수
 */
export function useClubRoundReportPage() {
  // 첫 화면 도서 요약을 전달받을 현재 라우팅 상태를 조회한다
  const location = useLocation();
  // 독후감 작성자 프로필 화면 이동에 사용할 라우터 함수를 조회한다
  const navigate = useNavigate();
  // 서버 조회와 접근 검증에 사용할 모임 및 회차 번호를 경로에서 조회한다
  const { clubNumb: clubNumbParam, rondNumb: rondNumbParam } = useParams();
  const clubNumb = Number(clubNumbParam);
  const rondNumb = Number(rondNumbParam);
  const isValidRoute = Number.isFinite(clubNumb)
    && clubNumb > 0
    && Number.isFinite(rondNumb)
    && rondNumb > 0;
  const [sort, setSort] = useState<PublicReportSortType>("LATEST_DESC");
  const [expandedReports, setExpandedReports] = useState<Record<number, boolean>>({});
  const [commentReport, setCommentReport] = useState<PublicReportType | null>(null);

  // 현재 활성 모임원에게 허용된 대상 회차 DONE 독후감 서버 페이지를 조회한다
  const reportsQuery = useReadingRoundReports(clubNumb, rondNumb, sort, isValidRoute);
  // 모임 회차 독후감 좋아요 변경 요청 상태를 조회한다
  const likeMutation = usePublicReportLike();
  const routeState = (location.state ?? {}) as ReportListBookSummary;
  const reportSummary = reportsQuery.data?.pages[0]?.data;
  const pageState: ReportListBookSummary = {
    title: reportSummary?.bookTitl ?? routeState.title,
    author: reportSummary?.bookAthr ?? routeState.author,
    cover: reportSummary?.bookCvim ?? routeState.cover,
    ratingAverage: reportSummary?.ratingAverage ?? routeState.ratingAverage,
  };

  // 조회된 회차별 서버 페이지를 화면 순서대로 하나의 DONE 독후감 목록으로 연결한다
  const reports = useMemo(() => {
    // 각 서버 페이지의 완료 독후감 목록을 연결해 반환한다
    return reportsQuery.data?.pages.flatMap(
      (page) => page.data?.reportPage.list ?? [],
    ) ?? [];
  }, [reportsQuery.data]);

  // 회차 DONE 독후감을 공통 카드 표시 모델로 변환한다
  const visibleReports = useMemo(() => {
    // 서버 상태 이름과 카드 펼침 상태를 적용한 표시 목록을 반환한다
    return createReportListItems(reports, expandedReports, EMPTY_STATUS_NAMES);
  }, [expandedReports, reports]);

  /**
   * 회차 독후감 목록의 정렬 기준을 변경한다.
   *
   * @author HanWon.Jang
   * @param nextSort 사용자가 선택한 정렬 기준
   * @return 반환값이 없다
   */
  const handleSortChange = (nextSort: PublicReportSortType): void => {
    // 변경한 정렬 기준으로 모임 회차 독후감을 다시 조회한다
    setSort(nextSort);
  };

  /**
   * 선택한 회차 독후감 본문의 펼침 상태를 반전한다.
   *
   * @author HanWon.Jang
   * @param reptNumb 펼침 상태를 변경할 독후감 번호
   * @return 반환값이 없다
   */
  const handleToggleReport = (reptNumb: number): void => {
    // 다른 카드 상태를 유지하면서 선택한 독후감의 펼침 상태만 변경한다
    setExpandedReports((previous) => ({
      ...previous,
      [reptNumb]: !previous[reptNumb],
    }));
  };

  /**
   * 회차 독후감 작성자의 소셜 프로필 화면으로 이동한다.
   *
   * @author HanWon.Jang
   * @param userNumb 이동할 작성자 사용자 번호
   * @return 반환값이 없다
   */
  const handleProfileClick = (userNumb: number): void => {
    // 유효한 작성자 번호만 소셜 프로필 경로에 사용한다
    if (userNumb > 0) {
      // 선택한 작성자의 소셜 프로필 화면으로 이동한다
      navigate(`/social/profile/${userNumb}`);
    }
  };

  /**
   * 선택한 회차 독후감의 좋아요 상태를 변경한다.
   *
   * @author HanWon.Jang
   * @param report 좋아요 대상을 식별할 회차 독후감
   * @return 반환값이 없다
   */
  const handleLike = (report: PublicReportType): void => {
    // 독후감 번호를 좋아요 API 요청 대상으로 전달한다
    likeMutation.mutate({
      tagtType: "REPORT",
      tagtNumb: report.reptNumb,
    });
  };

  /**
   * 선택한 회차 독후감의 댓글 바텀시트를 연다.
   *
   * @author HanWon.Jang
   * @param report 댓글을 조회할 회차 독후감
   * @return 반환값이 없다
   */
  const handleOpenReply = (report: PublicReportType): void => {
    // 선택한 독후감을 댓글 조회 대상으로 저장한다
    setCommentReport(report);
  };

  /**
   * 현재 열린 회차 독후감 댓글 바텀시트를 닫는다.
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleCloseReply = (): void => {
    // 댓글 조회 대상을 제거해 바텀시트 렌더링을 종료한다
    setCommentReport(null);
  };

  /**
   * 완료 회차 독후감 목록의 다음 서버 페이지를 조회한다.
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleLoadMore = (): void => {
    // 목록 하단에 도달하면 현재 회차와 정렬의 다음 페이지를 요청한다
    void reportsQuery.fetchNextPage();
  };

  // 모임 회차 독후감 전용 페이지가 사용할 상태와 처리 함수를 반환한다
  return {
    pageState,
    isValidRoute,
    isPending: reportsQuery.isPending,
    isError: reportsQuery.isError,
    error: reportsQuery.error,
    reportsCount: reports.length,
    visibleReports,
    sort,
    commentReport,
    isLikePending: likeMutation.isPending,
    hasNext: Boolean(reportsQuery.hasNextPage),
    isFetchingNext: reportsQuery.isFetchingNextPage,
    handleSortChange,
    handleToggleReport,
    handleProfileClick,
    handleLike,
    handleOpenReply,
    handleCloseReply,
    handleLoadMore,
  };
}
