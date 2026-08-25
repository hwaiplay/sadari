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
  likeCnt: number;
  likeYsno: "Y" | "N";
  replCnt: number;
};

/** 팔로잉 피드 한 페이지를 조회한다. */
export const getFeedPageApi = async (page: number): Promise<PageData<FeedItem>> => {
  const response = await api.get<ResultData<PageData<FeedItem>>>("/feed", { params: { page } });
  return assertResultDataSuccess(response.data).data as PageData<FeedItem>;
};
