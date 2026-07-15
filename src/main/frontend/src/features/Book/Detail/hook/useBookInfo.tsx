/**
 * src/main/frontend/src/features/Book/Detail/hook/useBookInfo.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
import { useQuery } from "@tanstack/react-query";
import { getBookInfoApi } from "../../api/bookApi";

export const useBookInfo = (reportNumb: number) => {
  return useQuery({
    queryKey: ["bookInfo", reportNumb],
    queryFn: async () => {
      const res = await getBookInfoApi(reportNumb);
      return res.data;
    },
    enabled: Number.isFinite(reportNumb),
  });
};