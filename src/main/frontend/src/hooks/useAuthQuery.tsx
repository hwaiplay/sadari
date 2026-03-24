import { useQuery } from "@tanstack/react-query";
import api from "../api/axios";

export const useAuthQuery = () => {
  return useQuery({
    queryKey: ["auth"],
    queryFn: async () => {
      const res = await api.get("/oauth/me");
      return res;
    },
    retry: false, // 실패 시 재시도 ❌
  });
};
