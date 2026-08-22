/**
 * src/main/frontend/src/features/Book/Detail/hook/usePublicReports.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
 */
import { useInfiniteQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { message } from "@/app/messages/message";
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import {
  getPublicReportsByIsbnApi,
  setPublicReportLikeApi,
  type PublicReportSortType,
} from "../../api/bookApi";

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
    mutationFn: setPublicReportLikeApi,
    onSuccess: () => {

      void queryClient.invalidateQueries({ queryKey: ["publicReports"] });
      void queryClient.invalidateQueries({ queryKey: ["readingClub"] });
      void queryClient.invalidateQueries({ queryKey: ["detail"] });
    },
    onError: (error: unknown) => {

      void sweetError(
        message("frontend.alert.updateFailedTitle"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    },
  });
};
