import api from "@/app/api/axios";
import { assertResultDataSuccess, type ResultData } from "@/app/api/resultData";

export type ServiceInfo = {
  cateCode: string;
  cateName: string;
  versNumb?: number | null;
  svciTitl?: string | null;
  svciCntn?: string | null;
  updtDate?: string | null;
  dplyDate?: string | null;
};

/**
 * 활성 서비스 정보 카테고리와 각 카테고리의 현재 배포본을 조회함
 *
 * @author HanWon.Jang
 * @return 서비스 정보 카테고리와 현재 배포본 목록
 */
export const getServiceInfoListApi = async (): Promise<ServiceInfo[]> => {

  // 인증 사용자의 서비스 정보 목록 API를 호출함
  const response = await api.get<ResultData<ServiceInfo[]>>("/service-info");
  const result = assertResultDataSuccess(response.data);
  // 배포본이 없는 카테고리를 포함한 서비스 정보 목록을 반환함
  return result.data ?? [];
};

/**
 * 로그인 전에 확인할 현재 배포 개인정보처리방침을 조회함
 *
 * @author HanWon.Jang
 * @return 현재 배포된 개인정보처리방침
 */
export const getPrivacyPolicyApi = async (): Promise<ServiceInfo | null> => {

  // 인증 없이 공개된 개인정보처리방침 API를 호출함
  const response = await api.get<ResultData<ServiceInfo | null>>("/service-info/privacy-policy");
  const result = assertResultDataSuccess(response.data);
  // 배포된 개인정보처리방침이 없으면 빈 상태를 반환함
  return result.data ?? null;
};
