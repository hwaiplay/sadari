/**
 * fileName       : useReplyLike
 * author         : HanWon.Jang
 * date           : 2026-08-03
 * description    : 댓글 좋아요 등록 및 취소 API와 댓글 목록 캐시 갱신을 처리하는 훅
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-03        HanWon.Jang        최초 생성
 */
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import {
  delReplyLikeApi,
  setReplyLikeApi,
} from "@/features/reply/api/replyApi";
import { REPLY_LIST_QUERY_KEY } from "@/features/reply/hooks/useReplyList";
import type {
  ReplyDtoType,
} from "@/features/reply/types/reply.types";
import { useMutation, useQueryClient } from "@tanstack/react-query";

type ReplyLikeRequest = Pick<
  ReplyDtoType,
  "reptNumb" | "replNumb" | "likeYsno"
>;

/**
 * 댓글 좋아요 등록과 취소 결과를 현재 댓글 목록 캐시에 반영한다
 *
 * @author HanWon.Jang
 * @param reptNumb 댓글 목록을 조회하는 독후감 번호
 * @return 댓글 좋아요 변경 이벤트와 진행 중인 댓글 번호
 */
export const useReplyLike = (reptNumb: number) => {
  // 댓글 목록 캐시를 서버의 좋아요 변경 결과와 동기화할 Query Client를 조회한다
  const queryClient = useQueryClient();

  /**
   * 서버가 반환한 댓글 좋아요 상태를 현재 댓글 목록의 동일 댓글에 반영한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const updateReplyLikeCache = (): void => {
    // 서버가 변경 결과를 확정한 댓글 목록 페이지만 다시 조회한다
    void queryClient.invalidateQueries({ queryKey: [REPLY_LIST_QUERY_KEY, reptNumb] });
  };

  /**
   * 댓글 좋아요 변경 실패 원인을 사용자에게 안내한다
   *
   * @author HanWon.Jang
   * @param error 댓글 좋아요 변경 중 발생한 오류
   * @return 반환값이 없다
   */
  const handleReplyLikeError = (error: unknown): void => {
    // "댓글 좋아요 처리에 실패했습니다."
    const failureTitle = message("frontend.reply.likeFailedTitle");
    // "다시 시도해주세요."
    const retryMessage = message("frontend.common.tryAgain");
    // 서버 응답에 사용자 메시지가 없으면 공통 재시도 문구를 오류 상세로 사용한다
    const errorMessage = getApiErrorMessage(error, retryMessage);
    // 서버의 안전한 오류 문구 또는 공통 재시도 안내를 오류 모달에 표시한다
    void sweetError(failureTitle, errorMessage);
  };

  // 댓글 좋아요 등록 요청과 성공 및 실패 상태를 관리한다
  const setReplyLikeMutation = useMutation({
    mutationFn: setReplyLikeApi,
    onSuccess: updateReplyLikeCache,
    onError: handleReplyLikeError,
  });
  // 댓글 좋아요 취소 요청과 성공 및 실패 상태를 관리한다
  const delReplyLikeMutation = useMutation({
    mutationFn: delReplyLikeApi,
    onSuccess: updateReplyLikeCache,
    onError: handleReplyLikeError,
  });

  /**
   * 현재 좋아요 여부에 따라 댓글 좋아요 등록 또는 취소 API를 호출한다
   *
   * @author HanWon.Jang
   * @param reply 변경할 댓글 식별값과 현재 좋아요 여부
   * @return 반환값이 없다
   */
  const handleToggleReplyLike = (reply: ReplyLikeRequest): void => {
    // 진행 중인 요청이 있으면 연속 클릭으로 반대 요청이 중복되지 않도록 차단한다
    if (setReplyLikeMutation.isPending || delReplyLikeMutation.isPending) {
      // 현재 좋아요 요청이 완료될 때까지 추가 처리를 종료한다
      return;
    }

    const request = {
      reptNumb: reply.reptNumb,
      replNumb: reply.replNumb,
    };

    // 이미 좋아요한 댓글이면 취소 API를 호출한다
    if (reply.likeYsno === "Y") {
      // 현재 사용자의 댓글 좋아요를 취소한다
      delReplyLikeMutation.mutate(request);
      // 한 번의 클릭에서 등록 요청까지 이어지지 않도록 종료한다
      return;
    }

    // 좋아요하지 않은 댓글이면 등록 API를 호출한다
    setReplyLikeMutation.mutate(request);
  };

  const isReplyLikePending =
    setReplyLikeMutation.isPending || delReplyLikeMutation.isPending;

  // 댓글 화면이 사용할 좋아요 변경 이벤트와 중복 요청 차단 상태를 반환한다
  return {
    isReplyLikePending,
    handleToggleReplyLike,
  };
};
