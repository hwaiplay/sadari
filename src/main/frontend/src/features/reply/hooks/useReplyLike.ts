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
  GetReplyListResponse,
  ReplyDtoType,
  ReplyLikeResponse,
  ReplyTarget,
} from "@/features/reply/types/reply.types";
import {
  type InfiniteData,
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";

type ReplyLikeRequest = Pick<
  ReplyDtoType,
  "replNumb" | "likeCnt" | "likeYsno"
>;

type ReplyListPages = InfiniteData<GetReplyListResponse>;
type ReplyLikeContext = {
  snapshot: ReplyListPages | undefined;
};

/**
 * 댓글 목록 캐시의 동일 댓글에 좋아요 상태를 병합한다
 *
 * @author HanWon.Jang
 * @param current 현재 댓글 무한 목록 캐시
 * @param replNumb 변경할 댓글 번호
 * @param detail 적용할 좋아요 수와 여부
 * @return 좋아요 상태가 반영된 댓글 목록 캐시
 */
const mergeReplyLike = (
  current: ReplyListPages | undefined,
  replNumb: number,
  detail: Pick<ReplyDtoType, "likeCnt" | "likeYsno">,
): ReplyListPages | undefined => {
  // 아직 조회되지 않은 댓글 캐시는 변경하지 않는다
  if (!current) {
    return current;
  }

  // 모든 조회 페이지에서 동일 댓글의 좋아요 상태만 변경한다
  return {
    ...current,
    pages: current.pages.map((page) => ({
      ...page,
      data: {
        ...page.data,
        list: page.data.list.map((reply) =>
          reply.replNumb === replNumb
            ? { ...reply, ...detail }
            : reply,
        ),
      },
    })),
  };
};

/**
 * 댓글 좋아요 등록과 취소 결과를 현재 댓글 목록 캐시에 반영한다
 *
 * @author HanWon.Jang
 * @param reptNumb 댓글 목록을 조회하는 독후감 번호
 * @return 댓글 좋아요 변경 이벤트와 진행 중인 댓글 번호
 */
export const useReplyLike = (target: ReplyTarget) => {
  // 댓글 목록 캐시를 서버의 좋아요 변경 결과와 동기화할 Query Client를 조회한다
  const queryClient = useQueryClient();
  const replyListQueryKey = [
    REPLY_LIST_QUERY_KEY,
    target.tagtType,
    target.tagtNumb,
  ] as const;

  /**
   * 현재 좋아요 여부에 맞는 등록 또는 취소 API를 호출한다
   *
   * @author HanWon.Jang
   * @param request 변경할 댓글 식별값과 현재 좋아요 상태
   * @return 서버가 확정한 댓글 좋아요 상태
   * @throws 댓글 좋아요 API 요청 또는 응답 검증 실패 시 발생
   */
  const requestReplyLike = async (
    request: ReplyLikeRequest,
  ): Promise<ReplyLikeResponse> => {
    const apiRequest = {
      ...target,
      replNumb: request.replNumb,
    };

    // 이미 좋아요한 댓글이면 취소 API를 호출한다
    return request.likeYsno === "Y"
      ? await delReplyLikeApi(apiRequest)
      : await setReplyLikeApi(apiRequest);
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

  // 댓글 좋아요의 즉시 화면 반영과 서버 확정 및 실패 원복 상태를 관리한다
  const replyLikeMutation = useMutation({
    mutationFn: requestReplyLike,
    /**
     * 서버 응답 전에 댓글 좋아요 상태를 화면 캐시에 반영한다
     *
     * @author HanWon.Jang
     * @param request 변경할 댓글과 현재 좋아요 상태
     * @return 실패 시 원복할 요청 전 댓글 목록 캐시
     */
    onMutate: async (request: ReplyLikeRequest): Promise<ReplyLikeContext> => {
      // 진행 중인 댓글 조회가 즉시 반영 상태를 덮어쓰지 않도록 취소한다
      await queryClient.cancelQueries({ queryKey: replyListQueryKey });
      const snapshot = queryClient.getQueryData<ReplyListPages>(replyListQueryKey);
      const isLiked = request.likeYsno === "Y";

      // 클릭 즉시 댓글 좋아요 여부와 수를 반전한다
      queryClient.setQueryData<ReplyListPages>(
        replyListQueryKey,
        (current) => mergeReplyLike(
          current,
          request.replNumb,
          {
            likeCnt: Math.max(0, (request.likeCnt ?? 0) + (isLiked ? -1 : 1)),
            likeYsno: isLiked ? "N" : "Y",
          },
        ),
      );

      // 핵심 요청 실패 시 사용할 이전 캐시를 반환한다
      return { snapshot };
    },
    /**
     * 서버가 확정한 댓글 좋아요 상태로 낙관적 값을 보정한다
     *
     * @author HanWon.Jang
     * @param result 댓글 좋아요 API 응답
     * @return 반환값이 없다
     */
    onSuccess: (result): void => {
      // 서버 응답의 확정 좋아요 상태를 동일 댓글에 반영한다
      queryClient.setQueryData<ReplyListPages>(
        replyListQueryKey,
        (current) => mergeReplyLike(current, result.data.replNumb, result.data),
      );
    },
    /**
     * 핵심 댓글 좋아요 요청 실패 시 화면을 원복하고 오류를 안내한다
     *
     * @author HanWon.Jang
     * @param error 댓글 좋아요 변경 중 발생한 오류
     * @param request 실패한 댓글 좋아요 요청
     * @param snapshot 요청 전 댓글 목록 캐시
     * @return 반환값이 없다
     */
    onError: (
      error: unknown,
      _request: ReplyLikeRequest,
      context: ReplyLikeContext | undefined,
    ): void => {
      // 요청 전 캐시가 있으면 핵심 요청 실패 상태를 화면에 원복한다
      if (context?.snapshot) {
        queryClient.setQueryData(replyListQueryKey, context.snapshot);
      }
      handleReplyLikeError(error);
    },
    /**
     * 댓글 좋아요 처리 종료 후 서버 최종 상태를 백그라운드에서 재확인한다
     *
     * @author HanWon.Jang
     * @return 반환값이 없다
     */
    onSettled: (): void => {
      // 화면을 막지 않고 현재 댓글 목록만 서버 상태와 동기화한다
      void queryClient.invalidateQueries({ queryKey: replyListQueryKey });
    },
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
    if (replyLikeMutation.isPending) {
      // 현재 좋아요 요청이 완료될 때까지 추가 처리를 종료한다
      return;
    }

    const request: ReplyLikeRequest = {
      replNumb: reply.replNumb,
      likeCnt: reply.likeCnt,
      likeYsno: reply.likeYsno,
    };
    // 현재 좋아요 여부에 맞는 등록 또는 취소 요청을 시작한다
    replyLikeMutation.mutate(request);
  };

  const isReplyLikePending = replyLikeMutation.isPending;

  // 댓글 화면이 사용할 좋아요 변경 이벤트와 중복 요청 차단 상태를 반환한다
  return {
    isReplyLikePending,
    handleToggleReplyLike,
  };
};
