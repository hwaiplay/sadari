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

/** 활성 서비스 정보 카테고리와 각 카테고리의 현재 배포본을 조회한다. */
export async function getServiceInfoListApi(): Promise<ServiceInfo[]> {

  // 인증 사용자의 서비스 정보 목록 API를 호출한다.
  const response = await api.get<ResultData<ServiceInfo[]>>("/service-info");
  const result = assertResultDataSuccess(response.data);
  // 배포본이 없는 카테고리를 포함한 서비스 정보 목록을 반환한다.
  return result.data ?? [];
}
