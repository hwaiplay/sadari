/**
 * src/main/frontend/src/features/Book/Detail/hook/usePublicReports.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
 */
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { message } from "@/app/messages/message";
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import {
  getPublicReportsByIsbnApi,
  setPublicReportLikeApi,
} from "../../api/bookApi";

/**
 * use Public Reports By Isbn 상태와 처리 함수를 제공한다
 *
 * @author HanWon.Jang
 * @param isbn isbn 입력값
 * @param enabled enabled 입력값
 * @return 화면에서 사용할 상태와 처리 함수
 */
export const usePublicReportsByIsbn = (isbn: string, enabled: boolean) => {

  return useQuery({
    queryKey: ["publicReports", "isbn", isbn],
    queryFn: async () => {

      return await getPublicReportsByIsbnApi(isbn);
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
