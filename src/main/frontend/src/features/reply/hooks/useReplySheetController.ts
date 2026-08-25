import { useDelReply } from "@/features/reply/hooks/useDelReply";
import { useReplyList } from "@/features/reply/hooks/useReplyList";
import { useReplyLike } from "@/features/reply/hooks/useReplyLike";
import { useReplySheet } from "@/features/reply/hooks/useReplySheet";
import { useSetReplyForm } from "@/features/reply/hooks/useSetReplyForm";
import type { ReplyDtoType } from "@/features/reply/types/reply.types";
import type { ReplyTarget } from "@/features/reply/types/reply.types";
import type { FocusEvent, KeyboardEvent } from "react";
import { useMemo, useRef, useState } from "react";

type UseReplySheetControllerProps = {
  target: ReplyTarget;
  onClose: () => void;
};

export type ReplyThread = {
  parentReply: ReplyDtoType;
  childReplies: ReplyDtoType[];
};

type ReplyCollection = {
  replyThreads: ReplyThread[];
  profilePathByNick: ReadonlyMap<string, string>;
};

/**
 * 댓글 등록 일시가 같을 때 댓글 번호까지 비교하여 답글의 시간순 정렬을 확정한다
 *
 * @author HanWon.Jang
 * @param firstReply 첫 번째 비교 대상 답글
 * @param secondReply 두 번째 비교 대상 답글
 * @return 첫 번째 답글의 정렬 우선순위
 */
const compareChildReplies = (
  firstReply: ReplyDtoType,
  secondReply: ReplyDtoType,
): number => {
  // 등록 일시를 먼저 비교하여 오래된 답글을 부모 댓글에 가깝게 배치한다
  const registeredDateOrder = firstReply.regiDate.localeCompare(
    secondReply.regiDate,
  );

  // 등록 일시가 다르면 날짜 비교 결과만으로 답글 순서를 결정한다
  if (registeredDateOrder !== 0) {
    // 등록 일시의 오름차순 비교 결과를 반환한다
    return registeredDateOrder;
  }

  // 동일한 등록 일시에는 댓글 번호가 작은 답글이 먼저 오도록 비교 결과를 반환한다
  return firstReply.replNumb - secondReply.replNumb;
};

/**
 * 자식 댓글을 등록 순서대로 정렬하여 부모 댓글 아래에 시간순으로 표시한다
 *
 * @author HanWon.Jang
 * @param childReplies 동일한 부모 댓글에 연결된 자식 댓글 목록
 * @return 등록 일시와 댓글 번호가 오래된 순서로 정렬된 새 배열
 */
const sortChildReplies = (
  childReplies: readonly ReplyDtoType[],
): ReplyDtoType[] => {
  // 서버 응답 배열을 변경하지 않고 답글 표시 순서만 정렬한 새 배열을 반환한다
  return [...childReplies].sort(compareChildReplies);
};

/**
 * 댓글 작성자와 로그인 사용자의 일치 여부에 따라 이동할 프로필 경로를 생성한다
 *
 * @author HanWon.Jang
 * @param userNumb 댓글 작성자 사용자 번호
 * @param myReplyYn 로그인 사용자가 작성한 댓글 여부
 * @return 본인이면 마이페이지, 다른 사용자이면 소셜 프로필 경로
 */
export const getReplyProfilePath = (
  userNumb: number,
  myReplyYn: "Y" | "N",
): string => {
  // 로그인 사용자가 작성한 댓글은 본인 프로필 편집 화면으로 연결한다
  if (myReplyYn === "Y") {
    // 마이페이지 프로필 경로를 반환한다
    return "/mypage/profile";
  }

  // 다른 사용자의 사용자 번호를 포함한 소셜 프로필 경로를 반환한다
  return `/social/profile/${userNumb}`;
};

/**
 * 서버 댓글 목록을 부모별 댓글 묶음과 닉네임별 프로필 경로로 변환한다
 *
 * @author HanWon.Jang
 * @param replies 서버에서 조회한 댓글 목록
 * @return 부모 댓글 묶음과 언급 링크용 프로필 경로
 */
const createReplyCollection = (
  replies: readonly ReplyDtoType[],
): ReplyCollection => {
  const parentReplies: ReplyDtoType[] = [];
  const childRepliesByParent = new Map<number, ReplyDtoType[]>();
  const profilePathByNick = new Map<string, string>();

  // 서버 목록을 부모 댓글과 부모 번호별 자식 댓글로 분리해 초기 화면에는 부모 댓글만 표시한다
  for (const reply of replies) {
    // 댓글 내용의 닉네임 언급을 사용자 프로필에 연결할 수 있도록 작성자 정보를 보관한다
    if (reply.userNick && reply.userNumb > 0) {
      // 본인 여부에 따라 생성한 프로필 경로를 중복되지 않는 닉네임과 연결한다
      profilePathByNick.set(
        reply.userNick,
        getReplyProfilePath(reply.userNumb, reply.myReplyYn),
      );
    }

    // 부모 여부가 명시된 댓글만 최상위 목록에 추가한다
    if (reply.parentYn === "Y") {
      // 부모 댓글 렌더링 순서는 서버가 반환한 정렬 순서를 유지한다
      parentReplies.push(reply);
      // 자식 댓글 분류를 이어서 처리한다
      continue;
    }

    // 부모 여부가 N이 아닌 데이터는 자식 댓글로 분류하지 않는다
    if (reply.parentYn !== "N") {
      // 정의되지 않은 부모 여부 값이 댓글 계층에 섞이지 않도록 다음 항목으로 이동한다
      continue;
    }

    // 부모 번호가 없는 비부모 데이터는 연결 대상을 확정할 수 없어 자식 목록에서 제외한다
    if (reply.uperNumb === null || reply.uperNumb === undefined) {
      // 잘못된 계층 데이터가 다른 댓글 아래에 표시되지 않도록 다음 항목으로 이동한다
      continue;
    }

    const childReplies = childRepliesByParent.get(reply.uperNumb) ?? [];
    // 동일한 부모 번호를 가진 답글을 서버 정렬 순서대로 누적한다
    childReplies.push(reply);
    // 부모 댓글 번호별 자식 댓글 목록을 최신 배열로 설정한다
    childRepliesByParent.set(reply.uperNumb, childReplies);
  }

  const replyThreads: ReplyThread[] = [];

  // 부모 댓글의 서버 정렬 순서를 유지하면서 연결된 답글을 표시 순서로 정리한다
  for (const parentReply of parentReplies) {
    // 부모 댓글과 시간순 답글 목록을 하나의 화면 단위로 추가한다
    replyThreads.push({
      parentReply,
      childReplies: sortChildReplies(
        childRepliesByParent.get(parentReply.replNumb) ?? [],
      ),
    });
  }

  // 댓글 화면이 반복 가공 없이 사용할 댓글 묶음과 프로필 경로를 반환한다
  return {
    replyThreads,
    profilePathByNick,
  };
};

/**
 * 댓글 바텀시트의 조회, 등록, 수정, 삭제, 좋아요 및 화면 상호작용 상태를 통합 관리한다
 *
 * @author HanWon.Jang
 * @param props 댓글을 조회할 독후감과 바텀시트 닫기 정보
 * @return 댓글 바텀시트 화면이 사용할 상태와 이벤트 처리 함수
 */
export const useReplySheetController = ({
  target,
  onClose,
}: UseReplySheetControllerProps) => {
  // 댓글 바텀시트의 닫기와 드래그 상호작용 속성을 조회한다
  const sheetInteraction = useReplySheet({ onClose });
  // 댓글 등록 API와 연결된 입력 상태와 제출 이벤트를 조회한다
  const replyForm = useSetReplyForm({
    ...target,
  });

  /**
   * 삭제된 댓글이 수정 중인 댓글과 같으면 하단 입력 폼을 일반 등록 상태로 되돌린다
   *
   * @author HanWon.Jang
   * @param deletedReplyNumb 삭제가 완료된 댓글 번호
   * @return 반환값이 없다
   */
  const handleReplyDeleted = (deletedReplyNumb: number): void => {
    // 현재 수정 중인 댓글이 삭제된 경우에만 수정 입력 상태를 초기화한다
    if (replyForm.editingReplyNumb === deletedReplyNumb) {
      // 삭제가 확정된 댓글의 수정 모드를 일반 댓글 등록 상태로 되돌린다
      replyForm.handleCancelEditReply();
    }
  };

  // 본인 댓글 삭제 확인과 API 요청 상태를 조회한다
  const deleteReply = useDelReply({
    ...target,
    onDeleted: handleReplyDeleted,
  });
  // 댓글 좋아요 등록과 취소 API 및 목록 캐시 갱신 상태를 조회한다
  const replyLike = useReplyLike(target);
  const commentInputRef = useRef<HTMLInputElement>(null);
  const [expandedReplyMap, setExpandedReplyMap] = useState<
    Record<number, boolean>
  >({});
  const [openActionReplyNumb, setOpenActionReplyNumb] = useState<
    number | null
  >(null);
  // 선택한 독후감의 댓글과 답글 목록을 서버 캐시에서 조회한다
  const replyListQuery = useReplyList(target);
  // 서버 페이지가 바뀔 때만 부모 댓글과 연결 답글을 정렬 순서대로 연결한다
  const replies = useMemo(
    () => replyListQuery.data?.pages.flatMap((page) => page.data.list) ?? [],
    [replyListQuery.data?.pages],
  );

  /**
   * 현재 서버 댓글 목록을 화면에서 사용할 부모별 댓글 묶음으로 구성한다
   *
   * @author HanWon.Jang
   * @return 부모별 댓글 묶음과 언급 링크용 프로필 경로
   */
  const getReplyCollection = (): ReplyCollection => {
    // 현재 조회 결과를 부모 댓글과 답글의 화면 구조로 변환하여 반환한다
    return createReplyCollection(replies);
  };

  // 서버 댓글 목록이 바뀔 때만 계층과 언급 링크 정보를 다시 계산한다
  const { replyThreads, profilePathByNick } = useMemo(
    getReplyCollection,
    [replies],
  );

  /**
   * 댓글 입력값 렌더링이 끝난 뒤 입력창의 포커스와 커서를 문자열 끝으로 이동한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const focusCommentInput = (): void => {
    const input = commentInputRef.current;

    // 바텀시트가 닫히지 않고 입력창이 남아 있을 때만 포커스를 이동한다
    if (input) {
      // 사용자가 댓글 내용을 바로 이어서 변경할 수 있도록 입력창에 포커스한다
      input.focus();
      // 현재 입력값 뒤에서 작성을 계속할 수 있도록 커서를 문자열 끝으로 이동한다
      input.setSelectionRange(input.value.length, input.value.length);
    }
  };

  /**
   * 선택한 부모 댓글의 자식 댓글 목록을 펼치거나 접는다
   *
   * @author HanWon.Jang
   * @param replNumb 자식 댓글 표시 상태를 변경할 부모 댓글 번호
   * @return 반환값이 없다
   */
  const handleToggleChildReplies = (replNumb: number): void => {
    /**
     * 다른 댓글의 펼침 상태를 유지하면서 선택한 댓글 상태만 반전한다
     *
     * @author HanWon.Jang
     * @param currentMap 현재 부모 댓글별 펼침 상태
     * @return 선택한 댓글의 상태만 반전된 새 객체
     */
    const getNextExpandedReplyMap = (
      currentMap: Record<number, boolean>,
    ): Record<number, boolean> => {
      // 선택한 부모 댓글의 펼침 여부만 반전한 새 상태를 반환한다
      return {
        ...currentMap,
        [replNumb]: !currentMap[replNumb],
      };
    };

    // 다른 부모 댓글의 펼침 상태를 유지하면서 선택한 댓글 상태만 반전한다
    setExpandedReplyMap(getNextExpandedReplyMap);
  };

  /**
   * 선택한 댓글 작성자를 언급하고 해당 댓글의 최상위 부모 번호로 답글 입력을 시작한다
   *
   * @author HanWon.Jang
   * @param reply 답글 대상 댓글 정보
   * @return 반환값이 없다
   */
  const handleReplyClick = (reply: ReplyDtoType): void => {
    const parentReplyNumb = reply.uperNumb ?? reply.replNumb;
    // 선택한 작성자 닉네임과 최상위 부모 번호를 답글 등록 상태에 반영한다
    replyForm.handleSelectReplyTarget(parentReplyNumb, reply.userNick);

    // 입력값 렌더링이 끝난 프레임에 답글 입력 위치를 설정한다
    window.requestAnimationFrame(focusCommentInput);
  };

  /**
   * 선택한 본인 댓글의 현재 내용을 입력창에 채우고 수정 모드를 시작한다
   *
   * @author HanWon.Jang
   * @param reply 수정할 본인 댓글 정보
   * @return 반환값이 없다
   */
  const handleEditReply = (reply: ReplyDtoType): void => {
    // 선택 메뉴가 수정 입력창 위에 남지 않도록 열린 메뉴를 닫는다
    setOpenActionReplyNumb(null);
    // 선택한 댓글 번호와 원문을 하단 댓글 입력 상태에 반영한다
    replyForm.handleStartEditReply(reply.replNumb, reply.replCntn, reply.editVersion);
    // 입력값 렌더링이 끝난 프레임에 수정 입력 위치를 설정한다
    window.requestAnimationFrame(focusCommentInput);
  };

  /**
   * 선택한 본인 댓글의 메뉴를 닫고 삭제 확인 및 API 처리를 시작한다
   *
   * @author HanWon.Jang
   * @param replNumb 삭제할 본인 댓글 번호
   * @return 반환값이 없다
   */
  const handleDeleteReply = (replNumb: number): void => {
    // 삭제 확인 모달을 표시하기 전에 댓글 액션 메뉴를 닫는다
    setOpenActionReplyNumb(null);
    // 사용자 확인 후 실제 댓글 삭제 API가 실행되도록 비동기 처리를 시작한다
    void deleteReply.handleDeleteReply(replNumb);
  };

  /**
   * 선택한 댓글의 수정과 삭제 또는 신고와 차단 액션 메뉴를 펼치거나 닫는다
   *
   * @author HanWon.Jang
   * @param replNumb 액션 메뉴를 변경할 댓글 번호
   * @return 반환값이 없다
   */
  const handleToggleActionMenu = (replNumb: number): void => {
    /**
     * 현재 열린 댓글과 선택한 댓글을 비교하여 다음 메뉴 상태를 결정한다
     *
     * @author HanWon.Jang
     * @param currentReplNumb 현재 액션 메뉴가 열린 댓글 번호
     * @return 새로 액션 메뉴를 열 댓글 번호 또는 닫힘 상태
     */
    const getNextActionReplyNumb = (
      currentReplNumb: number | null,
    ): number | null => {
      // 같은 댓글은 메뉴를 닫고 다른 댓글은 해당 메뉴를 열도록 다음 번호를 반환한다
      return currentReplNumb === replNumb ? null : replNumb;
    };

    // 하나의 댓글 액션 메뉴만 열리도록 현재 메뉴 상태를 변경한다
    setOpenActionReplyNumb(getNextActionReplyNumb);
  };

  /**
   * 댓글 액션 메뉴에서 포커스가 완전히 벗어나면 메뉴를 닫는다
   *
   * @author HanWon.Jang
   * @param event 댓글 액션 메뉴 영역의 포커스 이탈 이벤트
   * @return 반환값이 없다
   */
  const handleActionMenuBlur = (event: FocusEvent<HTMLDivElement>): void => {
    // 메뉴 내부의 다른 버튼으로 포커스가 이동하는 동안에는 열린 상태를 유지한다
    if (event.currentTarget.contains(event.relatedTarget)) {
      // 메뉴 내부 포커스 이동은 닫기 처리 없이 종료한다
      return;
    }

    // 메뉴 밖으로 포커스가 이동하면 현재 열린 댓글 액션 메뉴를 닫는다
    setOpenActionReplyNumb(null);
  };

  /**
   * Escape 키로 열린 댓글 액션 메뉴를 닫는다
   *
   * @author HanWon.Jang
   * @param event 댓글 액션 메뉴 영역의 키보드 이벤트
   * @return 반환값이 없다
   */
  const handleActionMenuKeyDown = (
    event: KeyboardEvent<HTMLDivElement>,
  ): void => {
    // Escape 키가 아닌 입력은 메뉴 내부 버튼의 기본 동작을 유지한다
    if (event.key !== "Escape") {
      // 별도 키보드 처리 없이 종료한다
      return;
    }

    // 브라우저의 추가 Escape 동작을 막고 현재 댓글 액션 메뉴를 닫는다
    event.preventDefault();
    // 열린 댓글 액션 메뉴 번호를 초기화한다
    setOpenActionReplyNumb(null);
  };

  /**
   * API가 연결되기 전 신고 또는 차단 메뉴 선택 시 열린 메뉴만 닫는다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleCloseActionMenu = (): void => {
    // 선택한 액션의 후속 API가 추가될 때까지 메뉴 표시 상태만 초기화한다
    setOpenActionReplyNumb(null);
  };

  // 댓글 바텀시트 화면에서 사용할 기능 상태와 이벤트를 반환한다
  return {
    ...sheetInteraction,
    ...replyForm,
    ...deleteReply,
    ...replyLike,
    commentInputRef,
    expandedReplyMap,
    openActionReplyNumb,
    replyListQuery,
    replyThreads,
    profilePathByNick,
    handleToggleChildReplies,
    handleReplyClick,
    handleEditReply,
    handleDeleteReply,
    handleToggleActionMenu,
    handleActionMenuBlur,
    handleActionMenuKeyDown,
    handleCloseActionMenu,
  };
};

export type ReplySheetController = ReturnType<typeof useReplySheetController>;
