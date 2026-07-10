import { useQuery } from "@tanstack/react-query";
import { getBookInfoApi } from "../../api/bookApi";

/**
 * 독후감에 연결된 책 상세 정보를 React Query로 조회한다.
 * @Author Hanwon.Jang
 * @param reportNumb 책 정보를 조회할 독후감 번호
 * @return 책 상세 정보 조회 Query 객체
 */
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
