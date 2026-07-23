/**
 * src/main/frontend/src/features/Common/utils/codeUtil.ts 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
import { useQuery } from "@tanstack/react-query";
import api from "@/app/api/axios";
import { assertResultDataSuccess } from "@/app/api/resultData";

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
  sortOrdr?: number;
};

export const getCodeListApi = async (commCode: string): Promise<CodeDetail[]> => {
  const res = await api.get(`/code/${commCode}`);
  return assertResultDataSuccess(res.data).data ?? [];
};

export const useCodeList = (commCode: string) => {
  return useQuery({
    queryKey: ["codeList", commCode],
    queryFn: () => getCodeListApi(commCode),
    staleTime: 1000 * 60 * 10,
  });
};
