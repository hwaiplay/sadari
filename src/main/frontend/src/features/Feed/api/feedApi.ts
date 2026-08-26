import api from "@/app/api/axios";
import { assertResultDataSuccess } from "@/app/api/resultData";
import type { PageData, ResultData } from "@/app/api/resultData";
import type { ReplyTargetType } from "@/features/reply/types/reply.types";

export type FeedItem = {
  tagtType: ReplyTargetType;
  tagtNumb: number;
  userNumb: number;
  userNick: string;
  porfPath?: string;
  activityDate: string;
  reptNumb?: number;
  reptStat?: "READ" | "DONE" | "STOP";
  reptStatName?: string;
  reptGrde?: string;
  reptCntn?: string;
  bookNumb?: number;
  bookTitl?: string;
  bookAthr?: string;
  bookCvim?: string;
  contentImagePath?: string;
  contentImageDisplayPath?: string;
  likeCnt: number;
  likeYsno: "Y" | "N";
  replCnt: number;
};

/**
 * 로그인 사용자가 팔로우하는 활성 사용자의 공개 활동 피드 한 페이지를 조회한다
 *
 * @author HanWon.Jang
 * @param page 조회할 피드 페이지 번호
 * @return 팔로잉 피드 목록과 현재 페이지 및 다음 페이지 여부
 * @throws API 요청 또는 공통 응답 검증 실패 시 발생
 */
export const getFeedPageApi = async (page: number): Promise<PageData<FeedItem>> => {
  // 서버가 인증 사용자와 페이지 조건으로 제한한 팔로잉 피드를 조회한다
  const response = await api.get<ResultData<PageData<FeedItem>>>("/feed", { params: { page } });
  // 공통 성공 코드가 검증된 피드 페이지 데이터만 화면에 반환한다
  return assertResultDataSuccess(response.data).data as PageData<FeedItem>;
};
