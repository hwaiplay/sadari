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

/**
 * 공통코드에 속한 사용 가능한 세부코드 목록을 조회한다.
 * @Author Hanwon.Jang
 * @param commCode 조회할 공통코드
 * @return 세부코드 목록
 */
export const getCodeListApi = async (commCode: string): Promise<CodeDetail[]> => {
  const res = await api.get(`/code/${commCode}`);
  return res.data.data ?? [];
};

/**
 * 공통코드 세부 목록을 React Query 캐시에 보관하며 조회한다.
 * @Author Hanwon.Jang
 * @param commCode 조회할 공통코드
 * @return 공통코드 목록 조회 Query 객체
 */
export const useCodeList = (commCode: string) => {
  return useQuery({
    queryKey: ["codeList", commCode],
    queryFn: () => getCodeListApi(commCode),
    staleTime: 1000 * 60 * 10,
  });
};
