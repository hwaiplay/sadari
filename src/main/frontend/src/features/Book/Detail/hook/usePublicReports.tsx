/**
 * src/main/frontend/src/features/Book/Detail/hook/usePublicReports.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
 */
import {
  type InfiniteData,
  type Query,
  type QueryClient,
  type QueryKey,
  useInfiniteQuery,
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";
import { message } from "@/app/messages/message";
import {
  getApiErrorMessage,
  type PageData,
  type ResultData,
} from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import {
  getPublicReportsByIsbnApi,
  setPublicReportLikeApi,
  type LikeDetail,
  type LikeTargetParams,
  type PublicReportSortType,
} from "../../api/bookApi";
import type {
  PublicReportType,
  ReportDetailType,
} from "../../types/book.type";
import type { ClubReadingRoundReportPage } from "@/features/ReadingClub/api/readingClubApi";

type PublicReportLikeRequest = LikeTargetParams & {
  likeCnt?: number;
  likeYsno?: "Y" | "N";
};

type PublicReportPages = InfiniteData<ResultData<PageData<PublicReportType>>>;
type ClubReportPages = InfiniteData<ResultData<ClubReadingRoundReportPage>>;

type PublicReportLikeContext = {
  publicReportSnapshots: Array<[QueryKey, PublicReportPages | undefined]>;
  clubReportSnapshots: Array<[QueryKey, ClubReportPages | undefined]>;
  detailSnapshot: ResultData<ReportDetailType> | undefined;
};

/**
 * React Query 항목이 모임 회차 독후감 무한 목록인지 확인한다
 *
 * @author HanWon.Jang
 * @param query 확인할 React Query 캐시 항목
 * @return 모임 회차 독후감 목록 여부
 */
const isClubReportQuery = (query: Query): boolean => {
  const queryKey = query.queryKey;

  // 모임 회차 보고서 키 구조와 일치하는 캐시만 좋아요 병합 대상으로 허용한다
  return queryKey[0] === "readingClub"
    && queryKey[2] === "readingRound"
    && queryKey[4] === "reports";
};

/**
 * 현재 좋아요 상태를 반전한 즉시 표시용 상태를 생성한다
 *
 * @author HanWon.Jang
 * @param request 좋아요 대상과 현재 화면 상태
 * @return 서버 응답 전 화면에 표시할 좋아요 상태
 */
const createOptimisticLike = (
  request: PublicReportLikeRequest,
): LikeDetail => {
  const isLiked = request.likeYsno === "Y";

  // 현재 상태를 반전하고 좋아요 수가 음수가 되지 않도록 보정한다
  return {
    likeCnt: Math.max(0, (request.likeCnt ?? 0) + (isLiked ? -1 : 1)),
    likeYsno: isLiked ? "N" : "Y",
  };
};

/**
 * 공개 독후감 한 건에 변경된 좋아요 상태를 병합한다
 *
 * @author HanWon.Jang
 * @param report 변경 여부를 확인할 공개 독후감
 * @param targetNumb 좋아요 대상 독후감 번호
 * @param detail 적용할 좋아요 상태
 * @return 대상이면 좋아요 상태가 변경된 독후감, 아니면 원본
 */
const mergeReportLike = (
  report: PublicReportType,
  targetNumb: number,
  detail: LikeDetail,
): PublicReportType => {
  // 동일한 독후감에만 좋아요 수와 사용자 상태를 반영한다
  return report.reptNumb === targetNumb
    ? { ...report, ...detail }
    : report;
};

/**
 * 일반 공개 독후감 무한 목록에 좋아요 상태를 반영한다
 *
 * @author HanWon.Jang
 * @param current 현재 공개 독후감 캐시
 * @param targetNumb 좋아요 대상 독후감 번호
 * @param detail 적용할 좋아요 상태
 * @return 좋아요 상태가 병합된 공개 독후감 캐시
 */
const mergePublicReportPages = (
  current: PublicReportPages | undefined,
  targetNumb: number,
  detail: LikeDetail,
): PublicReportPages | undefined => {
  // 아직 조회되지 않은 캐시는 변경하지 않는다
  if (!current) {
    return current;
  }

  // 모든 조회 페이지에서 동일 독후감의 좋아요 상태만 변경한다
  return {
    ...current,
    pages: current.pages.map((page) => ({
      ...page,
      data: page.data
        ? {
            ...page.data,
            list: page.data.list.map((report) =>
              mergeReportLike(report, targetNumb, detail),
            ),
          }
        : page.data,
    })),
  };
};

/**
 * 모임 회차 독후감 무한 목록에 좋아요 상태를 반영한다
 *
 * @author HanWon.Jang
 * @param current 현재 모임 회차 독후감 캐시
 * @param targetNumb 좋아요 대상 독후감 번호
 * @param detail 적용할 좋아요 상태
 * @return 좋아요 상태가 병합된 모임 회차 독후감 캐시
 */
const mergeClubReportPages = (
  current: ClubReportPages | undefined,
  targetNumb: number,
  detail: LikeDetail,
): ClubReportPages | undefined => {
  // 아직 조회되지 않은 캐시는 변경하지 않는다
  if (!current) {
    return current;
  }

  // 모든 회차 페이지에서 동일 독후감의 좋아요 상태만 변경한다
  return {
    ...current,
    pages: current.pages.map((page) => ({
      ...page,
      data: page.data
        ? {
            ...page.data,
            reportPage: {
              ...page.data.reportPage,
              list: page.data.reportPage.list.map((report) =>
                mergeReportLike(report, targetNumb, detail),
              ),
            },
          }
        : page.data,
    })),
  };
};

/**
 * 공개 독후감 목록과 모임 목록 및 상세 캐시에 좋아요 상태를 함께 반영한다
 *
 * @author HanWon.Jang
 * @param queryClient 변경할 React Query 캐시 관리자
 * @param targetNumb 좋아요 대상 독후감 번호
 * @param detail 적용할 좋아요 상태
 * @return 반환값이 없다
 */
const mergeLikeCaches = (
  queryClient: QueryClient,
  targetNumb: number,
  detail: LikeDetail,
): void => {
  // 일반 공개 독후감 목록 캐시에 변경 상태를 반영한다
  queryClient.setQueriesData<PublicReportPages>(
    { queryKey: ["publicReports"] },
    (current) => mergePublicReportPages(current, targetNumb, detail),
  );
  // 모임 회차 독후감 목록 캐시에 변경 상태를 반영한다
  queryClient.setQueriesData<ClubReportPages>(
    { predicate: isClubReportQuery },
    (current) => mergeClubReportPages(current, targetNumb, detail),
  );
  // 대상 독후감 상세 캐시에 변경 상태를 반영한다
  queryClient.setQueryData<ResultData<ReportDetailType>>(
    ["detail", targetNumb],
    (current) => current?.data
      ? { ...current, data: { ...current.data, ...detail } }
      : current,
  );
};

/**
 * use Public Reports By Isbn 상태와 처리 함수를 제공한다
 *
 * @author HanWon.Jang
 * @param isbn isbn 입력값
 * @param sortType 공개 독후감 정렬 코드
 * @param reptStat 공개 독후감 상태 필터
 * @param enabled enabled 입력값
 * @return 화면에서 사용할 상태와 처리 함수
 */
export const usePublicReportsByIsbn = (
  isbn: string,
  sortType: PublicReportSortType,
  reptStat: string,
  enabled: boolean,
) => {

  // ISBN과 정렬 및 상태별 공개 독후감 페이지를 하나의 무한 조회 캐시로 관리한다
  return useInfiniteQuery({
    queryKey: ["publicReports", "isbn", isbn, sortType, reptStat],
    /**
     * 현재 조건의 공개 독후감 한 페이지를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param context React Query가 전달한 현재 페이지 번호
     * @return 공개 독후감 페이지 응답
     * @throws 공개 독후감 API 요청 또는 응답 검증 실패 시 발생
     */
    queryFn: async ({ pageParam }) => {
      // 현재 페이지와 서버 검증 대상 필터를 함께 전달한다
      return await getPublicReportsByIsbnApi(isbn, sortType, reptStat, pageParam);
    },
    initialPageParam: 1,
    /**
     * 마지막 공개 독후감 응답에서 다음 페이지 번호를 계산한다
     *
     * @author SeungHyeon.Kang
     * @param lastPage 마지막으로 조회한 공개 독후감 페이지
     * @return 다음 페이지 번호 또는 조회 종료값
     */
    getNextPageParam: (lastPage) => {
      // 서버가 다음 페이지 존재를 확인한 경우에만 다음 번호를 반환한다
      return lastPage.data?.hasNext ? lastPage.data.page + 1 : undefined;
    },
    enabled: enabled && isbn.trim().length > 0,
  });
};

/**
 * use Public Report Like Mutation 상태와 처리 함수를 제공한다
 *
 * @author HanWon.Jang
 * @return 화면에서 사용할 상태와 처리 함수
 */
export const usePublicReportLike = () => {

  const queryClient = useQueryClient();

  return useMutation({
    /**
     * 화면 상태 필드를 제외한 좋아요 대상만 서버에 전달한다
     *
     * @author HanWon.Jang
     * @param request 좋아요 대상과 현재 화면 상태
     * @return 서버가 확정한 좋아요 변경 결과
     * @throws 좋아요 API 요청 또는 응답 검증 실패 시 발생
     */
    mutationFn: async (request: PublicReportLikeRequest) => {
      // 화면 낙관적 갱신용 값은 서버 요청 본문에서 제외한다
      return await setPublicReportLikeApi({
        tagtType: request.tagtType,
        tagtNumb: request.tagtNumb,
      });
    },
    /**
     * 서버 응답을 기다리지 않고 현재 좋아요 상태를 화면 캐시에 반영한다
     *
     * @author HanWon.Jang
     * @param request 좋아요 대상과 현재 화면 상태
     * @return 실패 시 원복할 이전 캐시 상태
     */
    onMutate: async (
      request: PublicReportLikeRequest,
    ): Promise<PublicReportLikeContext> => {
      // 진행 중인 동일 범위 조회가 즉시 반영 상태를 덮어쓰지 않도록 취소한다
      await Promise.all([
        queryClient.cancelQueries({ queryKey: ["publicReports"] }),
        queryClient.cancelQueries({ predicate: isClubReportQuery }),
        queryClient.cancelQueries({ queryKey: ["detail", request.tagtNumb] }),
      ]);

      const context: PublicReportLikeContext = {
        publicReportSnapshots:
          queryClient.getQueriesData<PublicReportPages>({
            queryKey: ["publicReports"],
          }),
        clubReportSnapshots:
          queryClient.getQueriesData<ClubReportPages>({
            predicate: isClubReportQuery,
          }),
        detailSnapshot:
          queryClient.getQueryData<ResultData<ReportDetailType>>([
            "detail",
            request.tagtNumb,
          ]),
      };

      // 클릭 즉시 좋아요 상태와 수를 모든 관련 화면 캐시에 반영한다
      mergeLikeCaches(
        queryClient,
        request.tagtNumb,
        createOptimisticLike(request),
      );

      // 핵심 좋아요 요청 실패 시 사용할 이전 캐시 상태를 반환한다
      return context;
    },
    /**
     * 서버가 확정한 좋아요 상태로 낙관적 값을 보정한다
     *
     * @author HanWon.Jang
     * @param result 좋아요 변경 API 응답
     * @param request 좋아요 대상과 기존 화면 상태
     * @return 반환값이 없다
     */
    onSuccess: (result, request): void => {
      // 서버가 반환한 확정 상태가 있으면 관련 캐시의 낙관적 값을 보정한다
      if (result.data) {
        mergeLikeCaches(queryClient, request.tagtNumb, result.data);
      }
    },
    /**
     * 핵심 좋아요 요청 실패 시 이전 화면 상태로 원복하고 오류를 안내한다
     *
     * @author HanWon.Jang
     * @param error 좋아요 변경 중 발생한 오류
     * @param request 실패한 좋아요 대상
     * @param context 요청 전에 저장한 캐시 상태
     * @return 반환값이 없다
     */
    onError: (
      error: unknown,
      request: PublicReportLikeRequest,
      context: PublicReportLikeContext | undefined,
    ): void => {
      // 일반 공개 독후감 캐시를 요청 전 상태로 되돌린다
      for (const [queryKey, snapshot] of context?.publicReportSnapshots ?? []) {
        queryClient.setQueryData(queryKey, snapshot);
      }
      // 모임 회차 독후감 캐시를 요청 전 상태로 되돌린다
      for (const [queryKey, snapshot] of context?.clubReportSnapshots ?? []) {
        queryClient.setQueryData(queryKey, snapshot);
      }
      // 상세 캐시가 요청 전에 존재한 경우 원래 상태로 되돌린다
      if (context?.detailSnapshot) {
        queryClient.setQueryData(
          ["detail", request.tagtNumb],
          context.detailSnapshot,
        );
      }

      void sweetError(
        message("frontend.alert.updateFailedTitle"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    },
    /**
     * 좋아요 처리 종료 후 서버 상태를 백그라운드에서 재확인한다
     *
     * @author HanWon.Jang
     * @return 반환값이 없다
     */
    onSettled: (): void => {
      // 현재 화면을 막지 않고 관련 조회 캐시를 서버 최종 상태와 동기화한다
      void queryClient.invalidateQueries({ queryKey: ["publicReports"] });
      void queryClient.invalidateQueries({ predicate: isClubReportQuery });
      void queryClient.invalidateQueries({ queryKey: ["detail"] });
    },
  });
};
