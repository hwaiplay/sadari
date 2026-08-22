/**
 * fileName       : useDelReply
 * author         : HanWon.Jang
 * date           : 2026-08-03
 * description    : 본인 댓글 삭제 확인과 API 요청 및 캐시 갱신을 처리하는 훅
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-03        HanWon.Jang        최초 생성
 */
import { getApiErrorMessage } from "@/app/api/resultData";
import {
  sweetConfirm,
  sweetError,
} from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { runBlockingOperation } from "@/app/navigation/blockingOperation";
import { delReplyApi } from "@/features/reply/api/replyApi";
import { REPLY_LIST_QUERY_KEY } from "@/features/reply/hooks/useReplyList";
import { useMutation, useQueryClient } from "@tanstack/react-query";

type UseDelReplyProps = {
  reptNumb: number;
  onDeleted?: (replNumb: number) => void;
};

/**
 * 본인 댓글 삭제를 확인한 뒤 논리 삭제 API를 호출하고 관련 조회 캐시를 갱신한다
 *
 * @author HanWon.Jang
 * @param props 삭제할 댓글이 속한 독후감 번호와 삭제 완료 처리 정보
 * @return 댓글 삭제 이벤트와 진행 중인 댓글 번호
 */
export const useDelReply = ({ reptNumb, onDeleted }: UseDelReplyProps) => {
  const queryClient = useQueryClient();
  // 댓글 삭제 요청의 진행 상태와 요청 변수를 관리한다
  const delReplyMutation = useMutation({
    mutationFn: delReplyApi,
  });

  /**
   * 사용자 확인 후 선택한 본인 댓글을 삭제 상태로 전환한다
   *
   * @author HanWon.Jang
   * @param replNumb 삭제할 댓글 번호
   * @return 삭제 확인과 API 처리 완료 Promise
   */
  const handleDeleteReply = async (replNumb: number): Promise<void> => {
    // 삭제 후 자동 복구되지 않는 동작임을 사용자에게 확인한다
    // "댓글을 삭제할까요?"
    const confirmTitle = message("frontend.reply.deleteConfirmTitle");
    // "삭제한 댓글은 다시 복구할 수 없어요."
    const confirmText = message("frontend.reply.deleteConfirmText");
    // 댓글 삭제 여부를 사용자가 선택할 수 있는 확인 모달을 표시한다
    const result = await sweetConfirm({
      title: confirmTitle,
      text: confirmText,
    });

    // 사용자가 취소하면 서버 데이터와 화면 캐시를 변경하지 않는다
    if (!result.isConfirmed) {
      // 삭제 취소 상태로 호출부에 반환한다
      return;
    }

    // API 실패도 사용자 안내 후 현재 화면에서 복구할 수 있도록 예외 경로를 분리한다
    try {
      /**
       * 댓글 삭제와 관련 화면 캐시 갱신을 하나의 차단 작업으로 실행한다
       *
       * @author SeungHyeon.Kang
       * @return 댓글 삭제 및 캐시 갱신 완료 Promise
       * @throws 댓글 삭제 또는 관련 캐시 갱신에 실패하면 발생한다
       */
      const deleteReplyAndRefresh = async (): Promise<void> => {
        // 로그인 사용자의 작성자 및 계정 상태를 검증하는 댓글 삭제 API를 호출한다
        await delReplyMutation.mutateAsync({ reptNumb, replNumb });
        // 삭제된 댓글을 참조하는 화면 입력 상태가 있으면 호출부에서 정리하도록 알린다
        onDeleted?.(replNumb);
        // 삭제 상태와 공개 독후감 댓글 수를 최신 서버 값으로 갱신한다
        await Promise.all([
          queryClient.invalidateQueries({
            queryKey: [REPLY_LIST_QUERY_KEY, reptNumb],
          }),
          queryClient.invalidateQueries({
            queryKey: ["publicReports"],
          }),
        ]);
      };

      // 삭제와 캐시 갱신 완료 후 처리 중 알림을 같은 삭제 성공 알림으로 전환한다
      await runBlockingOperation(deleteReplyAndRefresh, {
        success: {
          // "삭제되었습니다."
          title: message("frontend.alert.deleteSuccessTitle"),
        },
      });
    } catch (error: unknown) {
      // 삭제 실패 원인과 재시도 안내를 공통 오류 형식으로 표시한다
      // "댓글 삭제에 실패했습니다."
      await sweetError(
        message("frontend.reply.deleteFailedTitle"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    }
  };

  // 댓글 메뉴가 사용할 삭제 이벤트와 중복 요청 차단 상태를 반환한다
  return {
    deletingReplyNumb: delReplyMutation.isPending
      ? (delReplyMutation.variables?.replNumb ?? null)
      : null,
    handleDeleteReply,
  };
};
