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
 */
import api from "@/app/api/axios";
import { assertResultDataSuccess } from "@/app/api/resultData";
import type {
  GetReplyListResponse,
  ReplyDtoType,
  SetReplyResponse,
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
 * 독후감 번호에 연결된 댓글과 답글 목록을 조회한다
 *
 * @author HanWon.Jang
 * @param reptNumb 댓글 목록을 조회할 독후감 번호
 * @return 독후감에 등록된 댓글과 답글 목록 공통 응답
 * @throws 댓글 목록 조회 API 요청 또는 공통 응답 검증 실패 시 발생
 */
export const getReplyListApi = async (
  reptNumb: number,
): Promise<GetReplyListResponse> => {
  // 독후감 번호를 경로에 포함하여 해당 독후감의 댓글만 조회한다
  const response = await api.get<GetReplyListResponse>(`/reply/${reptNumb}`);

  // 서버가 반환한 공통 응답 코드가 성공인 경우에만 댓글 목록을 반환한다
  return assertResultDataSuccess(response.data);
};
