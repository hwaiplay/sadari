/**
 * fileName       : useSetReplyForm
 * author         : Hanwon.Jang
 * date           : 2026-07-28
 * description    : 댓글 등록 및 수정 폼 동작을 처리하는 훅
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        Hanwon.Jang        최초 생성
 * 2026-07-28        HanWon.Jang        댓글 등록 API와 입력 상태 연결
 * 2026-08-03        HanWon.Jang        댓글 수정 모드와 수정 API 연결
 */
import { useState } from "react";
import type { ChangeEvent, FormEvent } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { setReplyApi, uptReplyApi } from "@/features/reply/api/replyApi";
import { REPLY_LIST_QUERY_KEY } from "@/features/reply/hooks/useReplyList";
import type { ReplyTarget } from "@/features/reply/types/reply.types";

type UseSetReplyFormProps = ReplyTarget;

/**
 * 댓글 입력 상태를 관리하고 검증된 댓글을 등록하거나 수정한다
 *
 * @author HanWon.Jang
 * @param props 댓글을 등록할 독후감과 등록 성공 처리 정보
 * @return 댓글 입력값과 등록 폼 처리 상태
 */
export const useSetReplyForm = ({
  tagtType,
  tagtNumb,
}: UseSetReplyFormProps) => {
  const queryClient = useQueryClient();
  const [commentInput, setCommentInput] = useState("");
  const [uperNumb, setUperNumb] = useState<number | null>(null);
  const [editingReplyNumb, setEditingReplyNumb] = useState<number | null>(null);
  const [editingReplyVersion, setEditingReplyVersion] = useState<string | null>(null);

  /**
   * 댓글 입력값과 답글 및 수정 대상을 일반 댓글 등록 상태로 초기화한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const resetReplyForm = (): void => {
    // 이전 요청의 입력값과 계층 및 수정 식별값이 다음 등록에 재사용되지 않도록 초기화한다
    setCommentInput("");
    setUperNumb(null);
    setEditingReplyNumb(null);
    setEditingReplyVersion(null);
  };

  // 댓글 등록 요청의 진행 상태와 성공 및 실패 경로를 관리한다
  const setReplyMutation = useMutation({
    mutationFn: setReplyApi,
    /**
     * 등록된 댓글을 현재 화면에 반영하고 입력값을 초기화한다
     *
     * @author HanWon.Jang
     * @return 반환값이 없다
     */
    onSuccess: (): void => {
      // 연속 댓글 작성 시 이전 입력 상태가 다시 제출되지 않도록 초기화한다
      resetReplyForm();
      // 등록한 댓글이 현재 바텀시트 목록에 표시되도록 댓글 Query를 갱신한다
      void queryClient.invalidateQueries({
        queryKey: [REPLY_LIST_QUERY_KEY, tagtType, tagtNumb],
      });
      // 공개 독후감 카드의 댓글 수가 서버 값으로 갱신되도록 목록 Query를 갱신한다
      void queryClient.invalidateQueries({
        queryKey: ["publicReports"],
      });
    },
    /**
     * 댓글 등록 실패 원인을 사용자에게 안전한 문구로 안내한다
     *
     * @author HanWon.Jang
     * @param error 댓글 등록 중 발생한 오류
     * @return 반환값이 없다
     */
    onError: (error: unknown): void => {
      // "등록에 실패했습니다."
      void sweetError(
        message("frontend.alert.createFailedTitle"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    },
  });

  // 댓글 수정 요청의 진행 상태와 성공 및 실패 경로를 관리한다
  const uptReplyMutation = useMutation({
    mutationFn: uptReplyApi,
    /**
     * 수정된 댓글을 현재 목록에 반영하고 입력 폼을 일반 등록 상태로 되돌린다
     *
     * @author HanWon.Jang
     * @return 반환값이 없다
     */
    onSuccess: (): void => {
      // 수정한 원문과 식별값이 다음 댓글 등록에 재사용되지 않도록 입력 상태를 초기화한다
      resetReplyForm();
      // 수정한 댓글 내용과 수정 일시가 현재 바텀시트에 표시되도록 댓글 Query를 갱신한다
      void queryClient.invalidateQueries({
        queryKey: [REPLY_LIST_QUERY_KEY, tagtType, tagtNumb],
      });
    },
    /**
     * 댓글 수정 실패 원인을 사용자에게 안전한 문구로 안내한다
     *
     * @author HanWon.Jang
     * @param error 댓글 수정 중 발생한 오류
     * @return 반환값이 없다
     */
    onError: (error: unknown): void => {
      // "수정에 실패했습니다."
      void sweetError(
        message("frontend.alert.updateFailedTitle"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    },
  });

  /**
   * 댓글 입력 필드의 최신 값을 등록 폼 상태에 반영한다
   *
   * @author HanWon.Jang
   * @param event 댓글 입력 필드 변경 이벤트
   * @return 반환값이 없다
   */
  const handleCommentInputChange = (
    event: ChangeEvent<HTMLInputElement>,
  ): void => {
    // 사용자가 작성 중인 댓글 원문을 입력 필드에 유지한다
    setCommentInput(event.target.value);

    // 입력값을 모두 지우면 선택했던 답글 대상도 함께 해제한다
    if (event.target.value.length === 0) {
      // 다음 입력을 일반 댓글로 등록할 수 있도록 부모 댓글 번호를 초기화한다
      setUperNumb(null);
    }
  };

  /**
   * 선택한 댓글 작성자의 닉네임과 부모 댓글 번호를 답글 입력 상태에 반영한다
   *
   * @author HanWon.Jang
   * @param parentReplyNumb 답글이 연결될 최상위 부모 댓글 번호
   * @param userNick 입력창에서 언급할 댓글 작성자 닉네임
   * @return 반환값이 없다
   */
  const handleSelectReplyTarget = (
    parentReplyNumb: number,
    userNick: string,
  ): void => {
    // 답글 등록 요청에서 사용할 최상위 부모 댓글 번호를 설정한다
    setUperNumb(parentReplyNumb);
    // 답글 작성과 댓글 수정을 동시에 진행하지 않도록 수정 대상을 초기화한다
    setEditingReplyNumb(null);
    setEditingReplyVersion(null);
    // 사용자가 바로 답글 내용을 이어 쓸 수 있도록 언급 닉네임을 입력한다
    setCommentInput(`@${userNick} `);
  };

  /**
   * 선택한 댓글의 현재 내용을 입력창에 채우고 수정 모드를 시작한다
   *
   * @author HanWon.Jang
   * @param replNumb 수정할 댓글 번호
   * @param replCntn 수정 입력창에 표시할 현재 댓글 내용
   * @param editVersion 선택 시점의 댓글 원본 해시
   * @return 반환값이 없다
   */
  const handleStartEditReply = (replNumb: number, replCntn: string, editVersion: string): void => {
    // 수정 요청에서 사용할 댓글 번호를 설정한다
    setEditingReplyNumb(replNumb);
    // 저장 시 다른 탭의 선행 수정 여부를 비교할 원본 해시를 보관한다
    setEditingReplyVersion(editVersion);
    // 수정 중인 댓글을 답글로 잘못 등록하지 않도록 부모 번호를 초기화한다
    setUperNumb(null);
    // 사용자가 기존 원문을 바로 변경할 수 있도록 현재 댓글 내용을 입력한다
    setCommentInput(replCntn);
  };

  /**
   * 댓글 수정을 취소하고 입력 폼을 일반 댓글 등록 상태로 되돌린다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleCancelEditReply = (): void => {
    // 수정 대상과 입력 원문이 일반 댓글 등록에 남지 않도록 전체 입력 상태를 초기화한다
    resetReplyForm();
  };

  /**
   * 공백을 제거한 댓글 내용을 현재 입력 모드에 맞는 등록 또는 수정 API로 전송한다
   *
   * @author HanWon.Jang
   * @param event 댓글 등록 폼 제출 이벤트
   * @return 반환값이 없다
   */
  const handleSubmit = (event: FormEvent<HTMLFormElement>): void => {
    // 브라우저 기본 폼 전송으로 현재 페이지가 새로고침되지 않도록 차단한다
    event.preventDefault();

    const comment = commentInput.trim();

    // 공백 댓글과 진행 중인 등록 또는 수정 요청의 중복 제출은 서버에 전달하지 않는다
    if (
      comment.length === 0 ||
      setReplyMutation.isPending ||
      uptReplyMutation.isPending
    ) {
      // 등록할 수 없는 현재 입력 상태에서는 제출 처리를 종료한다
      return;
    }

    // 수정 대상이 있으면 복합 식별값과 정규화된 내용을 댓글 수정 API에 전달한다
    if (editingReplyNumb !== null && editingReplyVersion !== null) {
      // 작성 중인 댓글의 식별값과 변경할 내용을 수정 요청으로 전송한다
      uptReplyMutation.mutate({
        tagtType,
        tagtNumb,
        replNumb: editingReplyNumb,
        replCntn: comment,
        editVersion: editingReplyVersion,
      });
      // 같은 제출 이벤트에서 신규 댓글 등록까지 이어지지 않도록 종료한다
      return;
    }

    // 인증 쿠키를 사용하는 댓글 등록 API에 독후감 번호와 정규화된 내용을 전달한다
    setReplyMutation.mutate({
      tagtType,
      tagtNumb,
      replCntn: comment,
      uperNumb: uperNumb ?? undefined,
    });
  };

  const isSubmitDisabled =
    commentInput.trim().length === 0 ||
    setReplyMutation.isPending ||
    uptReplyMutation.isPending;

  // 댓글 입력 UI가 사용할 값과 등록 이벤트 및 요청 상태를 반환한다
  return {
    commentInput,
    editingReplyNumb,
    isSubmitDisabled,
    handleCommentInputChange,
    handleSelectReplyTarget,
    handleStartEditReply,
    handleCancelEditReply,
    handleSubmit,
  };
};
