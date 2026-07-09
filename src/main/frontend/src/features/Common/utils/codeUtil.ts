import { useQuery } from "@tanstack/react-query";
import api from "@/app/api/axios";

export type CodeDetail = {
  commCode: string;
  comdCode: string;
  comdName: string;
  codeExpl?: string;
  opt1Code?: string;
  opt1Name?: string;
  opt2Code?: string;
  opt2Name?: string;
  opt3Code?: string;
  opt3Name?: string;
  opt4Code?: string;
  opt4Name?: string;
  useeYsno?: string;
  sortOrder?: number;
};

export const getCodeListApi = async (commCode: string): Promise<CodeDetail[]> => {
  const res = await api.get(`/code/${commCode}`);
  return res.data.data ?? [];
};

export const useCodeList = (commCode: string) => {
  return useQuery({
    queryKey: ["codeList", commCode],
    queryFn: () => getCodeListApi(commCode),
    staleTime: 1000 * 60 * 10,
  });
};

