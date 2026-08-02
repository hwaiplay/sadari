import { createPortal } from "react-dom";
import { getApiErrorMessage } from "@/app/api/resultData";
import type { PublicReportType } from "@/features/Book/types/book.type";
import { message } from "@/app/messages/message";
import { useReplySheet } from "@/features/reply/hooks/useReplySheet";
import { useReplyList } from "@/features/reply/hooks/useReplyList";
import { useSetReplyForm } from "@/features/reply/hooks/useSetReplyForm";
import type { ReplyDtoType } from "@/features/reply/types/reply.types";
import { type ReactNode, useRef, useState } from "react";
import { Link } from "react-router-dom";
import * as styles from "./ReplySheet.css";

const DEFAULT_PROFILE_IMAGE = "/img/common/icon-user.svg";
const REPLY_MENTION_PATTERN = /(@[A-Za-z0-9\uAC00-\uD7A3]+)/g;

/**
 * 댓글 등록 일시를 댓글 목록에 표시할 두 자리 연도 날짜로 변환한다
 *
 * @author HanWon.Jang
 * @param value API에서 전달받은 댓글 등록 일시
 * @return yy.MM.dd 형식의 날짜 또는 변환할 수 없을 때 빈 문자열
 */
function formatReplyDate(value?: string): string {
  // 시간대 변환으로 날짜가 달라지지 않도록 ISO 문자열의 날짜 부분만 추출한다
  const dateMatch = value?.match(/^(\d{4})-(\d{2})-(\d{2})/);

  // 날짜 형식이 올바르지 않으면 잘못된 날짜 문구를 화면에 표시하지 않는다
  if (!dateMatch) {
    // 날짜를 표시하지 않도록 빈 문자열을 반환한다
    return "";
  }

  // 네 자리 연도의 뒤 두 자리와 월, 일을 점으로 연결해 반환한다
  return `${dateMatch[1].slice(-2)}.${dateMatch[2]}.${dateMatch[3]}`;
}

/**
 * 자식 댓글을 등록 순서대로 정렬하여 부모 댓글 아래에 시간순으로 표시한다
 *
 * @author HanWon.Jang
 * @param childReplies 동일한 부모 댓글에 연결된 자식 댓글 목록
 * @return 등록 일시와 댓글 번호가 오래된 순서로 정렬된 새 배열
 */
function sortChildReplies(
  childReplies: readonly ReplyDtoType[],
): ReplyDtoType[] {
  // 서버가 최신순으로 반환한 배열을 변경하지 않고 자식 댓글 전용 등록순 배열을 생성한다
  return [...childReplies].sort((firstReply, secondReply) => {
    const registeredDateOrder = firstReply.regiDate.localeCompare(
      secondReply.regiDate,
    );

    // 등록 일시가 다르면 오래된 자식 댓글을 부모 댓글에 더 가깝게 배치한다
    if (registeredDateOrder !== 0) {
      // 등록 일시의 오름차순 비교 결과를 반환한다
      return registeredDateOrder;
    }

    // 동일한 등록 일시에는 댓글 번호가 작은 자식 댓글을 먼저 배치한다
    return firstReply.replNumb - secondReply.replNumb;
  });
}

/**
 * 댓글 작성자와 로그인 사용자의 일치 여부에 따라 이동할 프로필 경로를 생성한다
 *
 * @author HanWon.Jang
 * @param userNumb 댓글 작성자 사용자 번호
 * @param myReplyYn 로그인 사용자가 작성한 댓글 여부
 * @return 본인이면 마이페이지, 다른 사용자이면 소셜 프로필 경로
 */
function getReplyProfilePath(
  userNumb: number,
  myReplyYn: "Y" | "N",
): string {
  // 로그인 사용자가 작성한 댓글은 본인 프로필 편집 화면으로 연결한다
  if (myReplyYn === "Y") {
    // 마이페이지 프로필 경로를 반환한다
    return "/mypage/profile";
  }

  // 다른 사용자의 사용자 번호를 포함한 소셜 프로필 경로를 반환한다
  return `/social/profile/${userNumb}`;
}

/**
 * 댓글 내용에서 프로필 경로가 확인된 언급을 사용자 프로필 링크로 변환한다
 *
 * @author HanWon.Jang
 * @param content 화면에 표시할 댓글 내용
 * @param profilePathByNick 댓글 목록의 닉네임별 프로필 경로
 * @return 일반 문구와 사용자 프로필 언급 링크로 구성된 댓글 내용
 */
function renderReplyContent(
  content: string,
  profilePathByNick: ReadonlyMap<string, string>,
): ReactNode[] {
  // 한글, 영문, 숫자로 구성된 닉네임 언급을 일반 문구와 분리한다
  const contentParts = content.split(REPLY_MENTION_PATTERN);
  const contentNodes: ReactNode[] = [];

  // 댓글 원문의 순서를 유지하면서 확인된 사용자 언급만 프로필 링크로 변환한다
  for (let partIndex = 0; partIndex < contentParts.length; partIndex += 1) {
    const contentPart = contentParts[partIndex];

    // 언급 형식이 아닌 문구는 원문 그대로 표시한다
    if (!contentPart.startsWith("@")) {
      // 일반 댓글 문구를 기존 위치에 추가한다
      contentNodes.push(contentPart);
      // 다음 댓글 문구를 이어서 처리한다
      continue;
    }

    const userNick = contentPart.slice(1);
    const profilePath = profilePathByNick.get(userNick);

    // 댓글 목록에서 사용자를 확인할 수 없는 언급은 잘못된 프로필로 연결하지 않는다
    if (profilePath === undefined) {
      // 확인되지 않은 언급도 언급 문구임을 구분할 수 있도록 브랜드 색상으로 추가한다
      contentNodes.push(
        <span
          className={styles.replyMentionLink}
          key={`mention-${partIndex}`}
        >
          {contentPart}
        </span>,
      );
      // 다음 댓글 문구를 이어서 처리한다
      continue;
    }

    // 확인된 사용자 번호를 포함한 소셜 프로필 링크를 댓글 내용에 추가한다
    contentNodes.push(
      <Link
        className={styles.replyMentionLink}
        key={`${profilePath}-${partIndex}`}
        to={profilePath}
      >
        {contentPart}
      </Link>,
    );
  }

  // 원문의 순서대로 구성된 댓글 내용 노드를 반환한다
  return contentNodes;
}

type ReplyItemRenderOptions = {
  isChild: boolean;
  childCount: number;
  isExpanded: boolean;
};

type ReplySheetProps = {
  report: Pick<PublicReportType, "reptNumb"> &
    Partial<Pick<PublicReportType, "userNick">>;
  onClose: () => void;
};

/**
 * 독후감 댓글 목록과 댓글 등록 폼을 바텀시트로 표시한다
 *
 * @author HanWon.Jang
 * @param props 댓글 바텀시트에 표시할 독후감과 댓글 정보
 * @return 댓글 목록과 등록 폼을 포함한 바텀시트
 */
function ReplySheet({
  report,
  onClose,
}: ReplySheetProps) {
  // 댓글 바텀시트의 닫기와 드래그 상호작용 속성을 조회한다
  const {
    sheetRef,
    sheetStyle,
    handleHandleKeyDown,
    handlePointerDown,
    handlePointerMove,
    handlePointerEnd,
  } = useReplySheet({ onClose });
  // 댓글 등록 API와 연결된 입력 상태와 제출 이벤트를 조회한다
  const {
    commentInput,
    isSubmitDisabled,
    handleCommentInputChange,
    handleSelectReplyTarget,
    handleSubmit,
  } = useSetReplyForm({
    reptNumb: report.reptNumb,
  });
  const commentInputRef = useRef<HTMLInputElement>(null);
  const [expandedReplyMap, setExpandedReplyMap] = useState<
    Record<number, boolean>
  >({});
  const [openActionReplyNumb, setOpenActionReplyNumb] = useState<
    number | null
  >(null);
  // 선택한 독후감의 댓글과 답글 목록을 서버 캐시에서 조회한다
  const replyListQuery = useReplyList(report.reptNumb);
  const replies = replyListQuery.data?.data ?? [];
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

  /**
   * 선택한 부모 댓글의 자식 댓글 목록을 펼치거나 접는다
   *
   * @author HanWon.Jang
   * @param replNumb 자식 댓글 표시 상태를 변경할 부모 댓글 번호
   * @return 반환값이 없다
   */
  const handleToggleChildReplies = (replNumb: number): void => {
    // 다른 부모 댓글의 펼침 상태를 유지하면서 선택한 댓글 상태만 반전한다
    setExpandedReplyMap((currentMap) => ({
      ...currentMap,
      [replNumb]: !currentMap[replNumb],
    }));
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
    handleSelectReplyTarget(parentReplyNumb, reply.userNick);
    // 입력값 렌더링이 끝난 뒤 포커스와 커서를 언급 닉네임 다음 위치로 이동한다
    window.requestAnimationFrame(() => {
      const input = commentInputRef.current;

      // 바텀시트가 닫히지 않고 입력창이 남아 있을 때만 포커스를 이동한다
      if (input) {
        // 사용자가 바로 답글 내용을 이어서 입력할 수 있도록 입력창에 포커스한다
        input.focus();
        // 언급 닉네임 뒤에서 입력을 계속할 수 있도록 커서를 문자열 끝으로 이동한다
        input.setSelectionRange(input.value.length, input.value.length);
      }
    });
  };

  /**
   * 선택한 댓글의 신고와 차단 액션 메뉴를 펼치거나 닫는다
   *
   * @author HanWon.Jang
   * @param replNumb 액션 메뉴를 변경할 댓글 번호
   * @return 반환값이 없다
   */
  const handleToggleActionMenu = (replNumb: number): void => {
    // 이미 열린 댓글이면 닫고 다른 댓글이면 해당 메뉴만 열리도록 상태를 변경한다
    setOpenActionReplyNumb((currentReplNumb) =>
      currentReplNumb === replNumb ? null : replNumb,
    );
  };

  /**
   * 댓글 액션 메뉴에서 포커스가 완전히 벗어나면 메뉴를 닫는다
   *
   * @author HanWon.Jang
   * @param event 댓글 액션 메뉴 영역의 포커스 이탈 이벤트
   * @return 반환값이 없다
   */
  const handleActionMenuBlur = (
    event: React.FocusEvent<HTMLDivElement>,
  ): void => {
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
    event: React.KeyboardEvent<HTMLDivElement>,
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

  /**
   * 부모 또는 자식 댓글 한 건의 작성자, 내용, 답글과 좋아요 제어 영역을 구성한다
   *
   * @author HanWon.Jang
   * @param reply 화면에 표시할 댓글 정보
   * @param options 자식 여부와 답글 목록 표시 상태
   * @return 구성된 댓글 항목
   */
  const renderReplyItem = (
    reply: ReplyDtoType,
    options: ReplyItemRenderOptions,
  ) => {
    // 댓글 등록 일시를 목록용 짧은 날짜 문구로 변환한다
    const formattedRegiDate = formatReplyDate(reply.regiDate);
    // 댓글 작성자의 본인 여부에 따라 프로필 이동 경로를 생성한다
    const profilePath = getReplyProfilePath(reply.userNumb, reply.myReplyYn);

    // 댓글 계층에 맞는 들여쓰기 스타일과 부모 전용 답글 더보기 제어를 포함한 항목을 반환한다
    return (
      /* 등록된 댓글 개별 항목 영역 */
      <article
        className={options.isChild
          ? styles.childReplyItem
          : styles.replyItem}
        key={`${report.reptNumb}-${reply.replNumb}`}
      >
        <div className={styles.replyItemWrap}>
          <div className={styles.replyWriterProfileImgArea}>
            {/* 댓글 작성자 프로필 사진 */}
            <Link
              className={styles.replyProfileLink}
              to={profilePath}
              aria-label={`${reply.userNick || "사용자"} 프로필 보기`}
            >
              <img
                className={styles.replyProfileImage}
                src={reply.porfPath || DEFAULT_PROFILE_IMAGE}
                alt=""
              />
            </Link>
          </div>
          <div className={styles.replyBody}>
            {/* 댓글 작성자 정보와 댓글 내용 영역 */}
            <div className={styles.replyTextArea}>
              {/* 댓글 작성자 닉네임과 작성일 영역 */}
              <div className={styles.replyWriterRow}>
                <Link
                  className={styles.replyWriter}
                  to={profilePath}
                >
                  {reply.userNick || "-"}
                </Link>
                {formattedRegiDate ? (
                  <time
                    className={styles.replyDate}
                    dateTime={reply.regiDate}
                  >
                    {formattedRegiDate}
                  </time>
                ) : null}
              </div>

              {/* 댓글 내용 */}
              <span className={styles.replyContent}>
                {renderReplyContent(reply.replCntn, profilePathByNick)}
              </span>
            </div>

            {/* 답글 작성과 자식 댓글 표시 제어 영역 */}
            <div className={styles.replyItemMetrics}>
              <button
                  className={styles.replyMetricButton}
                  type="button"
                  onClick={() => handleReplyClick(reply)}
              >
                {/* "답글 달기" */}
                답글 달기
              </button>
              {!options.isChild && options.childCount > 0 ? (
                  <>
                    {/* "답글 n개 더보기" 또는 "답글 숨기기" */}
                    <button
                        className={styles.replyMoreButton}
                        type="button"
                        aria-expanded={options.isExpanded}
                        aria-label={options.isExpanded
                            ? "답글 숨기기"
                            : `답글 ${options.childCount}개 더보기`}
                        onClick={() => handleToggleChildReplies(reply.replNumb)}
                    >
                  <span>
                    {options.isExpanded
                        ? "답글 숨기기"
                        : `답글 ${options.childCount}개 더보기`}
                  </span>
                    </button>
                  </>
              ) : null}
            </div>
          </div>
          </div>
        {/* 댓글 더보기와 좋아요 영역 */}
        <div className={styles.replyItemActions}>
          {reply.myReplyYn === "N" ? (
            /* 다른 사용자 댓글의 신고 및 차단 메뉴 영역 */
            <div
              className={styles.actionMenuRoot}
              onBlur={handleActionMenuBlur}
              onKeyDown={handleActionMenuKeyDown}
            >
              {/* "더보기" */}
              {/*<button*/}
              {/*  className={styles.actionMenuTrigger}*/}
              {/*  type="button"*/}
              {/*  aria-label="더보기"*/}
              {/*  aria-haspopup="menu"*/}
              {/*  aria-expanded={openActionReplyNumb === reply.replNumb}*/}
              {/*  onClick={() => handleToggleActionMenu(reply.replNumb)}*/}
              {/*>*/}
              {/*  <img*/}
              {/*    className={styles.actionMenuIcon}*/}
              {/*    src="/img/icons/icon-more.svg"*/}
              {/*    alt=""*/}
              {/*  />*/}
              {/*</button>*/}

              {openActionReplyNumb === reply.replNumb ? (
                /* 댓글 신고 및 사용자 차단 선택 메뉴 */
                <div className={styles.actionMenu} role="menu">
                  {/* "신고하기" */}
                  <button
                    className={styles.actionMenuOption}
                    type="button"
                    role="menuitem"
                    onClick={handleCloseActionMenu}
                  >
                    신고하기
                  </button>
                  {/* "차단하기" */}
                  <button
                    className={styles.actionMenuOption}
                    type="button"
                    role="menuitem"
                    onClick={handleCloseActionMenu}
                  >
                    차단하기
                  </button>
                </div>
              ) : null}
            </div>
          ) : null}

          {/* "좋아요" */}
          <button
            className={styles.replyLikeButton}
            type="button"
            aria-label="좋아요"
            aria-pressed={reply.likeYsno === "Y"}
          >
            {reply.likeYsno === "Y" ? (
              <img
                src="/img/icons/icon-heart-fill.svg"
                alt="좋아요"
                width="20"
              />
            ) : (
              <img
                src="/img/icons/icon-heart.svg"
                alt="좋아요"
                width="20"
              />
            )}
            <span>{Number(reply.likeCnt) || 0}</span>
          </button>
        </div>
      </article>
    );
  };

  const sheet = (
    /* 댓글 바텀시트 전체 영역 */
    <div className={styles.sheetLayer}>
      {/* 댓글 바텀시트 배경과 닫기 영역 */}
      {/* "닫기" */}
      <button
        className={styles.sheetBackdrop}
        type="button"
        aria-label={message("frontend.common.close")}
        onClick={onClose}
      />
      {/* 댓글 목록과 등록 기능을 제공하는 모달 본문 영역 */}
      {/* "사용자 닉네임님의 독후감 댓글" */}
      <section
        className={styles.commentSheet}
        ref={sheetRef}
        role="dialog"
        aria-modal="true"
        aria-label={report.userNick
          ? `${report.userNick}님의 독후감 댓글`
          : /* "댓글" */ message("frontend.report.comment.aria")}
        style={sheetStyle}
      >
        {/* 댓글 바텀시트 드래그와 키보드 닫기 영역 */}
        {/* "아래로 당겨 닫기" */}
        <div
          className={styles.sheetHandle}
          aria-label="아래로 당겨 닫기"
          role="button"
          tabIndex={0}
          onKeyDown={handleHandleKeyDown}
          onPointerDown={handlePointerDown}
          onPointerMove={handlePointerMove}
          onPointerUp={handlePointerEnd}
          onPointerCancel={handlePointerEnd}
        />

        {/* 댓글 목록과 빈 목록 안내 영역 */}
        <div className={styles.commentSheetBody}>
          {replyListQuery.isPending ? (
            /* 댓글 목록 조회 진행 상태 영역 */
            <div className={styles.commentEmpty}>
              <p className={styles.commentEmptyText}>
                {/* "목록 조회중" */}
                {message("frontend.common.loadingList")}
              </p>
            </div>
          ) : replyListQuery.isError ? (
            /* 댓글 목록 조회 실패 안내 영역 */
            <div className={styles.commentEmpty}>
              <p className={styles.commentEmptyText}>
                {getApiErrorMessage(
                  replyListQuery.error,
                  message("frontend.common.tryAgain"),
                )}
              </p>
            </div>
          ) : parentReplies.length > 0 ? (
            /* 등록된 댓글 목록 영역 */
            <div className={styles.replyList}>
              {parentReplies.map((reply) => {
                const childReplies = sortChildReplies(
                  childRepliesByParent.get(reply.replNumb) ?? [],
                );
                const isExpanded = Boolean(
                  expandedReplyMap[reply.replNumb],
                );

                // 부모 댓글과 사용자가 펼친 자식 댓글 목록을 하나의 댓글 묶음으로 반환한다
                return (
                  /* 부모 댓글과 연결된 자식 댓글 목록 영역 */
                  <div
                    className={styles.replyThread}
                    key={`${report.reptNumb}-thread-${reply.replNumb}`}
                  >
                    {renderReplyItem(reply, {
                      isChild: false,
                      childCount: childReplies.length,
                      isExpanded,
                    })}
                    {isExpanded ? (
                      /* 선택한 부모 댓글의 자식 댓글 목록 영역 */
                      <div className={styles.childReplyList}>
                        {childReplies.map((childReply) =>
                          renderReplyItem(childReply, {
                            isChild: true,
                            childCount: 0,
                            isExpanded: false,
                          }),
                        )}
                      </div>
                    ) : null}
                  </div>
                );
              })}
            </div>
          ) : (
            /* 댓글 빈 목록 안내 영역 */
            <div className={styles.commentEmpty}>
              <img
                className={styles.commentEmptyIcon}
                src="/img/icons/noti-REPLY.svg"
                alt=""
              />
              <p className={styles.commentEmptyTitle}>
                {/* "아직 댓글이 없어요." */}
                아직 댓글이 없어요.
              </p>
              <p className={styles.commentEmptyText}>
                {/* "첫 번째 댓글을 남겨보세요." */}
                첫 번째 댓글을 남겨보세요.
              </p>
            </div>
          )}
        </div>

        {/* 댓글 입력과 등록 버튼 영역 */}
        <form className={styles.commentForm} onSubmit={handleSubmit}>
          {/* "댓글을 입력해주세요." */}
          {/* "댓글 입력" */}
          <input
            ref={commentInputRef}
            className={styles.commentInput}
            type="text"
            value={commentInput}
            maxLength={500}
            placeholder="댓글을 입력해주세요."
            aria-label="댓글 입력"
            onChange={handleCommentInputChange}
          />
          <button
            className={styles.commentSubmitButton}
            type="submit"
            disabled={isSubmitDisabled}
          >
            {/* "등록" */}
            등록
          </button>
        </form>
      </section>
    </div>
  );

  // 브라우저 문서가 있으면 댓글 바텀시트를 최상위 body에 렌더링한다
  return typeof document !== "undefined"
    ? createPortal(sheet, document.body)
    : null;
}

export default ReplySheet;
