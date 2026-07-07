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
