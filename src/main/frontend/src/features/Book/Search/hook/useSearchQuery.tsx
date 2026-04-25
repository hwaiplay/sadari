/**
 * fileName       : useSearchQuery
 * author         : hanwon.Jang
 * date           : 2026-04-02
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-02       hanwon.Jang       최초 생성
 */

import { useQuery } from "@tanstack/react-query";
import api from "../../../../app/api/axios";

export const useSearchQuery = (searchKeyword: string) => {
  return useQuery({
    queryKey: ["search", searchKeyword],
    queryFn: async () => {
      try {
        const response = await api.get(
          `/book/search?query=${encodeURIComponent(searchKeyword)}`,
        );

        console.log(response.data);

        return response.data;
      } catch (err) {
        console.log("에러 발생:", err);
        throw err; // 중요 (Query가 에러 인식하도록)
      }
    },
  });
};
