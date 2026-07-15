/**
 * src/main/frontend/src/features/Book/Detail/hook/usePublicReports.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  getPublicReportsByIsbnApi,
  getPublicReportsByReportApi,
  setPublicReportLikeApi,
} from "../../api/bookApi";

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