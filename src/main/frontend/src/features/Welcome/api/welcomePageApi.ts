import api from "@/app/api/axios";
import { assertResultDataSuccess, type ResultData } from "@/app/api/resultData";

export type WelcomeManagedPage = {
  wlcmNumb: number;
  versNumb: number;
  subxTitl: string;
  mainTitl: string;
  pageDesc: string;
  imgeUrlx: string | null;
  sortOrdr: number;
};

/** 현재 배포 중인 관리자 웰컴페이지를 노출 순서대로 조회함 */
export const getWelcomePageListApi = async (): Promise<WelcomeManagedPage[]> => {
  // 인증 사용자의 현재 배포 웰컴페이지 목록 API를 호출함
  const response = await api.get<ResultData<WelcomeManagedPage[]>>("/welcome-pages");
  const result = assertResultDataSuccess(response.data);
  // 배포 순서가 적용된 관리자 웰컴페이지 목록을 반환함
  return result.data ?? [];
};
