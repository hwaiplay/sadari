/**
 * src/main/frontend/src/features/Common/utils/codeUtil.ts 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당함
 *
 * @author HanWon.Jang
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

export type CodeGroupList = Record<string, CodeDetail[]>;

/**
 * 요청 순서나 중복 여부가 달라도 동일한 React Query 캐시 키를 사용하도록 공통코드 목록을 정규화함
 *
 * @author HanWon.Jang
 * @param commCodeList 조회할 공통코드 목록
 * @returns 중복을 제거하고 오름차순으로 정렬한 공통코드 목록
 */
const getNormalizedCommCodeList = (
  commCodeList: readonly string[],
): string[] => {

  return [
    ...new Set(
      commCodeList
        .map((commCode) => commCode.trim().toUpperCase())
        .filter((commCode) => commCode.length > 0),
    ),
  ].sort();
};

/**
 * 하나의 공통코드에 속한 세부코드 목록을 조회함
 *
 * @author HanWon.Jang
 * @param commCode 조회할 공통코드
 * @returns 세부코드 목록
 */
export const getCodeListApi = async (commCode: string): Promise<CodeDetail[]> => {

  const res = await api.get(`/code/${commCode}`);
  return assertResultDataSuccess(res.data).data ?? [];
};

/**
 * 여러 공통코드에 속한 세부코드를 한 번의 API 요청으로 조회함
 *
 * @author HanWon.Jang
 * @param commCodeList 조회할 공통코드 목록
 * @returns 공통코드를 키로 사용하는 세부코드 목록
 */
export const getCodeGroupListApi = async (
  commCodeList: readonly string[],
): Promise<CodeGroupList> => {

  const normalizedCommCodeList = getNormalizedCommCodeList(commCodeList);

  // 빈 코드 목록은 백엔드의 IN 조건을 호출하지 않고 빈 그룹으로 즉시 반환함
  if (normalizedCommCodeList.length === 0) {
    return {};
  }

  const res = await api.get("/code", {
    params: {
      commCodes: normalizedCommCodeList.join(","),
    },
  });
  return assertResultDataSuccess(res.data).data ?? {};
};

/**
 * 단일 공통코드 목록을 React Query 캐시에 저장하여 재사용함
 *
 * @author HanWon.Jang
 * @param commCode 조회할 공통코드
 * @returns 단일 공통코드 조회 Query 객체
 */
export const useCodeList = (commCode: string) => {

  return useQuery({
    queryKey: ["codeList", commCode],
    queryFn: () => getCodeListApi(commCode),
    staleTime: 1000 * 60 * 10,
  });
};

/**
 * 여러 공통코드 목록을 하나의 React Query 요청 및 캐시 항목으로 관리함
 *
 * @author HanWon.Jang
 * @param commCodeList 조회할 공통코드 목록
 * @returns 공통코드 일괄 조회 Query 객체
 */
export const useCodeGroupList = (commCodeList: readonly string[]) => {

  const normalizedCommCodeList = getNormalizedCommCodeList(commCodeList);

  return useQuery({
    queryKey: ["codeGroupList", normalizedCommCodeList],
    queryFn: () => getCodeGroupListApi(normalizedCommCodeList),
    enabled: normalizedCommCodeList.length > 0,
    staleTime: 1000 * 60 * 10,
  });
};
