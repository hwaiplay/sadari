/**
 * fileName       : reply.types
 * author         : Hanwon.Jang
 * date           : 2026-07-28
 * description    : 댓글 관련 타입들을 정의
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        Hanwon.Jang    최초 생성
 * 2026-07-28        Hanwon.Jang    댓글 정보 타입 정의
 * 2026-07-29        HanWon.Jang    댓글 계층과 수정 상태 타입 정의
 * 2026-07-29        HanWon.Jang    로그인 사용자 작성 댓글 여부 타입 정의
 * 2026-08-03        HanWon.Jang    댓글 수정 및 삭제 API 응답 타입 정의
 * 2026-08-03        HanWon.Jang    댓글 좋아요 API 응답 타입 정의
 */

import type { PageData, ResultData } from "@/app/api/resultData";

export type ReplyTargetType = "REPORT" | "PROFILE_IMAGE" | "BACKGROUND_IMAGE";

export type ReplyTarget = {
    tagtType: ReplyTargetType;
    tagtNumb: number;
};

/**
 * 댓글 정보 타입
 */
export interface ReplyDtoType {
    // 댓글 대상 유형
    tagtType: ReplyTargetType;
    // 댓글 대상 번호
    tagtNumb: number;
    // 댓글이 작성된 독후감 번호
    reptNumb: number;
    // 독후감별 댓글 번호
    replNumb: number;
    // 답글이 참조하는 부모 댓글 번호
    uperNumb?: number | null;
    // 부모 댓글 여부
    parentYn: "Y" | "N";
    // 댓글 작성자 사용자 번호
    userNumb: number;
    // 로그인 사용자가 작성한 댓글 여부
    myReplyYn: "Y" | "N";
    // 댓글 또는 답글 내용
    replCntn: string;
    // 댓글 삭제 여부
    deltYsno: string;
    // 댓글 등록 일시
    regiDate: string;
    // 댓글 수정 일시
    updtDate?: string | null;
    // 다른 탭이나 기기의 선행 수정 여부를 확인할 원본 내용 해시
    editVersion: string;
    // 댓글 수정 여부
    updtYsno: "Y" | "N";
    // 댓글 수정 여부에 따라 화면에 표시할 문구
    updtYsnoNm?: string;
    // 유저 닉네임
    userNick: string;
    // 프로필 사진
    porfPath?: string;
    // 댓글 좋아요 개수
    likeCnt?: number;
    // 로그인 사용자의 댓글 좋아요 여부
    likeYsno?: "Y" | "N";
    // 댓글에 등록된 답글 개수
    replCnt?: number;
}

/**
 * 댓글 등록 후 리턴 타입
 */
export type SetReplyResponse = ResultData<number> & {
    data: number;
};

/**
 * 댓글 수정 후 리턴 타입
 */
export type UptReplyResponse = ResultData<number> & {
    data: number;
};

/**
 * 댓글 삭제 후 리턴 타입
 */
export type DelReplyResponse = ResultData<number> & {
    data: number;
};

/**
 * 댓글 좋아요 등록 또는 취소 후 리턴 타입
 */
export type ReplyLikeResponse = ResultData<
    Pick<ReplyDtoType, "tagtType" | "tagtNumb" | "replNumb" | "likeCnt" | "likeYsno">
> & {
    data: Pick<
        ReplyDtoType,
        "tagtType" | "tagtNumb" | "replNumb" | "likeCnt" | "likeYsno"
    >;
};

/**
 * 댓글 목록 조회 후 리턴 타입
 */
export type GetReplyListResponse = ResultData<PageData<ReplyDtoType>> & {
    data: PageData<ReplyDtoType>;
};
