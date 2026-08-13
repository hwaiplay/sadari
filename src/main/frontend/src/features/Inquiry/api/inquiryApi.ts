import api from "@/app/api/axios";
import { assertResultDataSuccess, type ResultData } from "@/app/api/resultData";

export type InquiryAnswer = {
  answNumb: number;
  answCntn: string;
  readYsno: "Y" | "N";
  regiDate: string;
};

export type Inquiry = {
  inqrNumb: number;
  inqrCatg: string;
  inqrCatgName: string;
  inqrTitl: string;
  inqrCntn?: string;
  inqrStat: string;
  inqrStatName: string;
  spndNumb?: number | null;
  unreadCount: number;
  regiDate: string;
  answDate?: string | null;
  answers?: InquiryAnswer[];
};

export type InquiryPage = {
  list: Inquiry[];
  page: number;
  hasNext: boolean;
};

export type InquiryCreate = {
  inqrCatg: string;
  inqrTitl: string;
  inqrCntn: string;
};

/**
 * 인증 사용자의 고객문의 목록을 조회한다
 *
 * @author SeungHyeon.Kang
 * @param page 조회할 페이지 번호
 * @return 본인 고객문의 목록 페이지
 */
export async function getInquiryListApi(page: number): Promise<InquiryPage> {

  const response = await api.get<ResultData<InquiryPage>>("/inquiries", { params: { page } });
  const result = assertResultDataSuccess(response.data);
  if (!result.data) {
    throw new Error("고객문의 목록이 없습니다.");
  }
  return result.data;
}

/**
 * 인증 사용자가 작성한 고객문의 상세를 조회한다
 *
 * @author SeungHyeon.Kang
 * @param inqrNumb 조회할 고객문의 번호
 * @return 고객문의 본문과 관리자 답변
 */
export async function getInquiryDetailApi(inqrNumb: number): Promise<Inquiry> {

  const response = await api.get<ResultData<Inquiry>>(`/inquiries/${inqrNumb}`);
  const result = assertResultDataSuccess(response.data);
  if (!result.data) {
    throw new Error("고객문의를 찾을 수 없습니다.");
  }
  return result.data;
}

/**
 * 인증 사용자의 새 고객문의를 접수한다
 *
 * @author SeungHyeon.Kang
 * @param inquiry 등록할 고객문의 값
 * @return 등록된 고객문의 번호
 */
export async function setInquiryApi(inquiry: InquiryCreate): Promise<number> {

  const response = await api.post<ResultData<number>>("/inquiries", inquiry);
  const result = assertResultDataSuccess(response.data);
  if (!result.data) {
    throw new Error("고객문의 번호를 확인할 수 없습니다.");
  }
  return result.data;
}
