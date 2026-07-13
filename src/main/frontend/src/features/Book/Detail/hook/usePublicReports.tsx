import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  getPublicReportsByIsbnApi,
  getPublicReportsByReportApi,
  setPublicReportLikeApi,
} from "../../api/bookApi";

/**
 * 기준 독후감 번호로 같은 도서의 공개 독후감 목록을 조회한다.
 * @Author Hanwon.Jang
 * @param reportNumb 기준 독후감 번호
 * @param enabled 조회 실행 여부
 * @return 공개 독후감 목록 Query 객체
 */
export const usePublicReportsByReport = (
  reportNumb: number,
  enabled: boolean,
) => {
  return useQuery({
    queryKey: ["publicReports", "report", reportNumb],
    queryFn: async () => {
      const res = await getPublicReportsByReportApi(reportNumb);
      return res.data;
    },
    enabled: enabled && Number.isFinite(reportNumb),
  });
};

/**
 * ISBN으로 같은 도서의 공개 독후감 목록을 조회한다.
 * @Author Hanwon.Jang
 * @param isbn 도서 ISBN
 * @param enabled 조회 실행 여부
 * @return 공개 독후감 목록 Query 객체
 */
export const usePublicReportsByIsbn = (isbn: string, enabled: boolean) => {
  return useQuery({
    queryKey: ["publicReports", "isbn", isbn],
    queryFn: async () => {
      const res = await getPublicReportsByIsbnApi(isbn);
      return res.data;
    },
    enabled: enabled && isbn.trim().length > 0,
  });
};

/**
 * 공개 독후감 좋아요 토글 API를 호출하고 공개 독후감 목록을 다시 조회한다.
 * @Author Hanwon.Jang
 * @return 공개 독후감 좋아요 Mutation 객체
 */
export const usePublicReportLikeMutation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: setPublicReportLikeApi,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["publicReports"] });
      void queryClient.invalidateQueries({ queryKey: ["detail"] });
    },
  });
};
