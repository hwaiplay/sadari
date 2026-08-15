/**
 * fileName       : reply
 * author         : Hanwon.Jang
 * date           : 2026-07-28
 * description    : 댓글과 답글 API 요청 및 응답 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        Hanwon.Jang        최초 생성
 * 2026-07-28        Hanwon.Jang        댓글 등록 API 계약 연결
 * 2026-08-03        HanWon.Jang        본인 댓글 수정 및 삭제 API 계약 연결
 * 2026-08-03        HanWon.Jang        댓글 좋아요 등록 및 취소 API 계약 연결
 */
import api from "@/app/api/axios";
import { assertResultDataSuccess } from "@/app/api/resultData";
import type {
  DelReplyResponse,
  GetReplyListResponse,
  ReplyDtoType,
  ReplyLikeResponse,
  SetReplyResponse,
  UptReplyResponse,
} from "@/features/reply/types/reply.types";

/**
 * 로그인 사용자가 작성한 댓글 또는 답글을 등록한다
 *
 * @author HanWon.Jang
 * @param data 등록할 독후감 번호와 댓글 내용
 * @return 등록된 댓글 번호를 포함한 공통 응답
 * @throws 댓글 등록 API 요청 또는 공통 응답 검증 실패 시 발생
 */
export const setReplyApi = async (
  data: Pick<ReplyDtoType, "reptNumb" | "replCntn"> & {
    uperNumb?: number;
  },
): Promise<SetReplyResponse> => {
  // 로그인 사용자 정보는 인증 쿠키에서 확인하고 댓글 요청 데이터만 서버에 전달한다
  const response = await api.post<SetReplyResponse>("/reply", data);

  // 서버가 반환한 공통 응답 코드가 성공인 경우에만 등록 결과를 반환한다
  return assertResultDataSuccess(response.data);
};

/**
 * 로그인 사용자가 작성한 댓글 또는 답글의 내용을 수정한다
 *
 * @author HanWon.Jang
 * @param data 수정할 독후감 번호와 댓글 번호 및 내용
 * @return 수정된 댓글 번호를 포함한 공통 응답
 * @throws 댓글 수정 API 요청 또는 공통 응답 검증 실패 시 발생
 */
export const uptReplyApi = async (
  data: Pick<ReplyDtoType, "reptNumb" | "replNumb" | "replCntn" | "editVersion">,
): Promise<UptReplyResponse> => {
  // 복합 식별값을 경로에 포함하고 검증할 댓글 내용만 서버에 전달한다
  const response = await api.put<UptReplyResponse>(
    `/reply/${data.reptNumb}/${data.replNumb}`,
    { replCntn: data.replCntn, editVersion: data.editVersion },
  );

  // 서버가 반환한 공통 응답 코드가 성공인 경우에만 수정 결과를 반환한다
  return assertResultDataSuccess(response.data);
};

/**
 * 로그인 사용자가 작성한 댓글 또는 답글을 삭제 상태로 전환한다
 *
 * @author HanWon.Jang
 * @param data 삭제할 독후감 번호와 댓글 번호
 * @return 삭제된 댓글 번호를 포함한 공통 응답
 * @throws 댓글 삭제 API 요청 또는 공통 응답 검증 실패 시 발생
 */
export const delReplyApi = async (
  data: Pick<ReplyDtoType, "reptNumb" | "replNumb">,
): Promise<DelReplyResponse> => {
  // 복합 식별값을 경로에 포함하여 작성자와 계정 상태를 서버에서 검증한다
  const response = await api.delete<DelReplyResponse>(
    `/reply/${data.reptNumb}/${data.replNumb}`,
  );

  // 서버가 반환한 공통 응답 코드가 성공인 경우에만 삭제 결과를 반환한다
  return assertResultDataSuccess(response.data);
};

/**
 * 로그인 사용자의 미삭제 댓글 좋아요를 등록한다
 *
 * @author HanWon.Jang
 * @param data 좋아요 대상 댓글의 독후감 번호와 댓글 번호
 * @return 변경 후 댓글 좋아요 상태와 좋아요 수
 * @throws 댓글 좋아요 등록 API 요청 또는 공통 응답 검증 실패 시 발생
 */
export const setReplyLikeApi = async (
  data: Pick<ReplyDtoType, "reptNumb" | "replNumb">,
): Promise<ReplyLikeResponse> => {
  // 댓글 복합 식별값을 경로에 포함하여 좋아요 등록 API를 호출한다
  const response = await api.put<ReplyLikeResponse>(
    `/reply/${data.reptNumb}/${data.replNumb}/likes`,
  );

  // 서버가 반환한 공통 응답 코드가 성공인 경우에만 변경 상태를 반환한다
  return assertResultDataSuccess(response.data);
};

/**
 * 로그인 사용자의 미삭제 댓글 좋아요를 취소한다
 *
 * @author HanWon.Jang
 * @param data 좋아요 대상 댓글의 독후감 번호와 댓글 번호
 * @return 변경 후 댓글 좋아요 상태와 좋아요 수
 * @throws 댓글 좋아요 취소 API 요청 또는 공통 응답 검증 실패 시 발생
 */
export const delReplyLikeApi = async (
  data: Pick<ReplyDtoType, "reptNumb" | "replNumb">,
): Promise<ReplyLikeResponse> => {
  // 댓글 복합 식별값을 경로에 포함하여 좋아요 취소 API를 호출한다
  const response = await api.delete<ReplyLikeResponse>(
    `/reply/${data.reptNumb}/${data.replNumb}/likes`,
  );

  // 서버가 반환한 공통 응답 코드가 성공인 경우에만 변경 상태를 반환한다
  return assertResultDataSuccess(response.data);
};


/**
 * 독후감 번호에 연결된 댓글과 답글 목록을 조회한다
 *
 * @author HanWon.Jang
 * @param reptNumb 댓글 목록을 조회할 독후감 번호
 * @param page 조회할 부모 댓글 페이지 번호
 * @return 독후감에 등록된 댓글과 답글 목록 공통 응답
 * @throws 댓글 목록 조회 API 요청 또는 공통 응답 검증 실패 시 발생
 */
export const getReplyListApi = async (
  reptNumb: number,
  page: number,
): Promise<GetReplyListResponse> => {
  // 독후감 번호를 경로에 포함하여 해당 독후감의 댓글만 조회한다
  const response = await api.get<GetReplyListResponse>(`/reply/${reptNumb}`, {
    params: { page },
  });

  // 서버가 반환한 공통 응답 코드가 성공인 경우에만 댓글 목록을 반환한다
  return assertResultDataSuccess(response.data);
};
