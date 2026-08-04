import { getApiErrorMessage } from "@/app/api/resultData";
import { message } from "@/app/messages/message";
import Loading from "@/components/Loading/Loading";
import type { PublicReportType } from "@/features/Book/types/book.type";
import type {
  ReplySheetController,
  ReplyThread as ReplyThreadType,
} from "@/features/reply/hooks/useReplySheetController";
import { getReplyProfilePath } from "@/features/reply/hooks/useReplySheetController";
import type { ReplyDtoType } from "@/features/reply/types/reply.types";
import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import * as styles from "./ReplySheet.css";

const DEFAULT_PROFILE_IMAGE = "/img/common/icon-user.svg";
const REPLY_MENTION_PATTERN = /(@[A-Za-z0-9\uAC00-\uD7A3]+)/g;

type ReplySheetViewProps = {
  report: Pick<PublicReportType, "reptNumb"> &
    Partial<Pick<PublicReportType, "userNick">>;
  onClose: () => void;
  controller: ReplySheetController;
};

type ReplyItemProps = {
  reportNumb: number;
  reply: ReplyDtoType;
  isChild: boolean;
  childCount: number;
  isExpanded: boolean;
  controller: ReplySheetController;
};

type ReplyThreadProps = {
  reportNumb: number;
  thread: ReplyThreadType;
  controller: ReplySheetController;
};

/**
 * 댓글 등록 일시를 댓글 목록에 표시할 두 자리 연도 날짜로 변환한다
 *
 * @author HanWon.Jang
 * @param value API에서 전달받은 댓글 등록 일시
 * @return yy.MM.dd 형식의 날짜 또는 변환할 수 없을 때 빈 문자열
 */
const formatReplyDate = (value?: string): string => {
  // 시간대 변환으로 날짜가 달라지지 않도록 ISO 문자열의 날짜 부분만 추출한다
  const dateMatch = value?.match(/^(\d{4})-(\d{2})-(\d{2})/);

  // 날짜 형식이 올바르지 않으면 잘못된 날짜 문구를 화면에 표시하지 않는다
  if (!dateMatch) {
    // 날짜를 표시하지 않도록 빈 문자열을 반환한다
    return "";
  }

  // 네 자리 연도의 뒤 두 자리와 월, 일을 점으로 연결해 반환한다
  return `${dateMatch[1].slice(-2)}.${dateMatch[2]}.${dateMatch[3]}`;
};

/**
 * 댓글 내용에서 프로필 경로가 확인된 언급을 사용자 프로필 링크로 변환한다
 *
 * @author HanWon.Jang
 * @param content 화면에 표시할 댓글 내용
 * @param profilePathByNick 댓글 목록의 닉네임별 프로필 경로
 * @return 일반 문구와 사용자 프로필 언급 링크로 구성된 댓글 내용
 */
const renderReplyContent = (
  content: string,
  profilePathByNick: ReadonlyMap<string, string>,
): ReactNode[] => {
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
};

/**
 * 부모 또는 자식 댓글 한 건의 작성자와 내용 및 제어 영역을 표시한다
 *
 * @author HanWon.Jang
 * @param props 댓글 정보와 계층 및 상호작용 상태
 * @return 작성자와 내용 및 답글 제어를 포함한 댓글 항목
 */
const ReplyItem = ({
  reportNumb,
  reply,
  isChild,
  childCount,
  isExpanded,
  controller,
}: ReplyItemProps) => {
  // 댓글 등록 일시를 목록용 짧은 날짜 문구로 변환한다
  const formattedRegiDate = formatReplyDate(reply.regiDate);
  // 댓글 작성자의 본인 여부에 따라 프로필 이동 경로를 생성한다
  const profilePath = getReplyProfilePath(reply.userNumb, reply.myReplyYn);
  // 삭제된 댓글의 원문과 상호작용 제어를 화면에 노출하지 않도록 상태를 구분한다
  const isDeleted = reply.deltYsno === "Y";

  /**
   * 현재 댓글을 답글 대상으로 선택한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleReplyClick = (): void => {
    // 현재 댓글의 작성자와 최상위 부모 번호를 답글 입력 상태에 반영한다
    controller.handleReplyClick(reply);
  };

  /**
   * 현재 부모 댓글의 답글 목록을 펼치거나 접는다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleToggleChildReplies = (): void => {
    // 현재 부모 댓글 번호의 답글 표시 상태만 변경한다
    controller.handleToggleChildReplies(reply.replNumb);
  };

  /**
   * 현재 본인 댓글의 원문을 하단 입력창에서 수정하도록 선택한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleEditClick = (): void => {
    // 현재 댓글 정보로 수정 모드를 시작한다
    controller.handleEditReply(reply);
  };

  /**
   * 현재 본인 댓글의 삭제 확인과 API 처리를 시작한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleDeleteClick = (): void => {
    // 현재 댓글 번호를 삭제 확인 대상으로 전달한다
    controller.handleDeleteReply(reply.replNumb);
  };

  /**
   * 현재 댓글의 액션 메뉴를 펼치거나 닫는다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleActionMenuClick = (): void => {
    // 현재 댓글 번호의 액션 메뉴 표시 상태만 변경한다
    controller.handleToggleActionMenu(reply.replNumb);
  };

  /**
   * 현재 댓글의 좋아요 상태에 따라 등록 또는 취소 처리를 시작한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleLikeClick = (): void => {
    // 현재 댓글 식별값과 좋아요 상태를 기능 컨트롤러에 전달한다
    controller.handleToggleReplyLike(reply);
  };

  // 댓글 계층에 맞는 들여쓰기와 답글 및 좋아요 제어를 포함한 항목을 반환한다
  return (
    /* 등록된 댓글 개별 항목 영역 */
    <article
      className={isChild ? styles.childReplyItem : styles.replyItem}
      key={`${reportNumb}-${reply.replNumb}`}
    >
      <div className={styles.replyItemWrap}>
        {/* 댓글 작성자 프로필 이미지 영역 */}
        <div className={styles.replyWriterProfileImgArea}>
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

        {/* 댓글 작성자 정보와 댓글 내용 영역 */}
        <div className={styles.replyBody}>
          <div className={styles.replyTextArea}>
            {/* 댓글 작성자 닉네임과 작성일 영역 */}
            <div className={styles.replyWriterRow}>
              <Link className={styles.replyWriter} to={profilePath}>
                {reply.userNick || "-"}
              </Link>
              {formattedRegiDate ? (
                <time className={styles.replyDate} dateTime={reply.regiDate}>
                  {formattedRegiDate}
                </time>
              ) : null}
            </div>

            {/* 댓글 내용 영역 */}
            <span
              className={
                isDeleted
                  ? styles.deletedReplyContent
                  : styles.replyContent
              }
            >
              {isDeleted ? (
                <>
                  {/* "삭제된 댓글입니다." */}
                  {message("frontend.reply.deletedContent")}
                </>
              ) : (
                renderReplyContent(
                    reply.replCntn,
                    controller.profilePathByNick,
                  )
              )}
            </span>
          </div>

          {/* 답글 작성과 자식 댓글 표시 제어 영역 */}
          <div className={styles.replyItemMetrics}>
            {!isDeleted ? (
              <button
                className={styles.replyMetricButton}
                type="button"
                onClick={handleReplyClick}
              >
                {/* "답글 달기" */}
                답글 달기
              </button>
            ) : null}
            {!isChild && childCount > 0 ? (
              <button
                className={styles.replyMoreButton}
                type="button"
                aria-expanded={isExpanded}
                aria-label={
                  isExpanded ? "답글 숨기기" : `답글 ${childCount}개 더보기`
                }
                onClick={handleToggleChildReplies}
              >
                <span>
                  {/* "답글 n개 더보기" 또는 "답글 숨기기" */}
                  {isExpanded
                    ? "답글 숨기기"
                    : `답글 ${childCount}개 더보기`}
                </span>
              </button>
            ) : null}
          </div>
        </div>
      </div>

      {/* 댓글 더보기와 좋아요 영역 */}
      <div className={styles.replyItemActions}>
        {!isDeleted ? (
          /* 댓글 작성자 일치 여부에 맞는 선택 메뉴 영역 */
          <div
            className={styles.actionMenuRoot}
            onBlur={controller.handleActionMenuBlur}
            onKeyDown={controller.handleActionMenuKeyDown}
          >
            {/* "더보기" */}
            <button
              className={styles.actionMenuTrigger}
              type="button"
              aria-label="더보기"
              aria-haspopup="menu"
              aria-expanded={controller.openActionReplyNumb === reply.replNumb}
              onClick={handleActionMenuClick}
            >
              <img
                className={styles.actionMenuIcon}
                src="/img/icons/icon-more.svg"
                alt=""
              />
            </button>

            {controller.openActionReplyNumb === reply.replNumb ? (
              /* 본인 댓글 수정 및 삭제 또는 다른 사용자 신고 및 차단 선택 메뉴 */
              <div className={styles.actionMenu} role="menu">
                {reply.myReplyYn === "Y" ? (
                  <>
                    {/* "수정하기" */}
                    <button
                      className={styles.actionMenuOption}
                      type="button"
                      role="menuitem"
                      onClick={handleEditClick}
                    >
                      {/* "수정하기" */}
                      {message("frontend.reply.edit")}
                    </button>
                    {/* "삭제하기" */}
                    <button
                      className={styles.actionMenuOptionDanger}
                      type="button"
                      role="menuitem"
                      disabled={
                        controller.deletingReplyNumb === reply.replNumb
                      }
                      onClick={handleDeleteClick}
                    >
                      {/* "삭제하기" */}
                      {message("frontend.reply.delete")}
                    </button>
                  </>
                ) : (
                  <>
                    {/* "신고하기" */}
                    <button
                      className={styles.actionMenuOption}
                      type="button"
                      role="menuitem"
                      onClick={controller.handleCloseActionMenu}
                    >
                      신고하기
                    </button>
                    {/* "차단하기" */}
                    <button
                      className={styles.actionMenuOption}
                      type="button"
                      role="menuitem"
                      onClick={controller.handleCloseActionMenu}
                    >
                      차단하기
                    </button>
                  </>
                )}
              </div>
            ) : null}
          </div>
        ) : null}

        {!isDeleted ? (
          /* "좋아요" */
          <button
            className={styles.replyLikeButton}
            type="button"
            aria-label={
              reply.likeYsno === "Y"
                ? /* "좋아요 취소" */ message("frontend.reply.unlikeAria")
                : /* "좋아요" */ message("frontend.reply.likeAria")
            }
            aria-pressed={reply.likeYsno === "Y"}
            disabled={controller.isReplyLikePending}
            onClick={handleLikeClick}
          >
            {reply.likeYsno === "Y" ? (
              <img
                src="/img/icons/icon-heart-fill.svg"
                alt=""
                width="20"
              />
            ) : (
              <img src="/img/icons/icon-heart.svg" alt="" width="20" />
            )}
            <span>{Number(reply.likeCnt) || 0}</span>
          </button>
        ) : null}
      </div>
    </article>
  );
};

/**
 * 부모 댓글과 사용자가 펼친 자식 댓글 목록을 하나의 댓글 묶음으로 표시한다
 *
 * @author HanWon.Jang
 * @param props 부모 댓글 묶음과 화면 상호작용 상태
 * @return 부모 댓글과 선택적으로 펼쳐진 답글 목록
 */
const ReplyThread = ({
  reportNumb,
  thread,
  controller,
}: ReplyThreadProps) => {
  const isExpanded = Boolean(
    controller.expandedReplyMap[thread.parentReply.replNumb],
  );

  /**
   * 자식 댓글 한 건을 부모 댓글 아래의 들여쓰기 항목으로 표시한다
   *
   * @author HanWon.Jang
   * @param childReply 화면에 표시할 자식 댓글
   * @return 자식 댓글 화면 항목
   */
  const renderChildReply = (childReply: ReplyDtoType): ReactNode => {
    // 부모 댓글의 독후감 번호와 공통 상호작용 상태를 적용한 자식 댓글을 반환한다
    return (
      <ReplyItem
        key={`${reportNumb}-${childReply.replNumb}`}
        reportNumb={reportNumb}
        reply={childReply}
        isChild
        childCount={0}
        isExpanded={false}
        controller={controller}
      />
    );
  };

  // 부모 댓글과 현재 펼침 상태에 맞는 답글 목록을 반환한다
  return (
    /* 부모 댓글과 연결된 자식 댓글 목록 영역 */
    <div
      className={styles.replyThread}
      key={`${reportNumb}-thread-${thread.parentReply.replNumb}`}
    >
      <ReplyItem
        reportNumb={reportNumb}
        reply={thread.parentReply}
        isChild={false}
        childCount={thread.childReplies.length}
        isExpanded={isExpanded}
        controller={controller}
      />
      {isExpanded ? (
        /* 선택한 부모 댓글의 자식 댓글 목록 영역 */
        <div className={styles.childReplyList}>
          {thread.childReplies.map(renderChildReply)}
        </div>
      ) : null}
    </div>
  );
};

/**
 * 기능 로직에서 전달한 댓글 상태를 바텀시트 화면으로 렌더링한다
 *
 * @author HanWon.Jang
 * @param props 독후감 정보와 닫기 처리 및 댓글 기능 상태
 * @return 댓글 목록과 등록 폼으로 구성된 바텀시트 화면
 */
const ReplySheetView = ({
  report,
  onClose,
  controller,
}: ReplySheetViewProps) => {
  /**
   * 부모 댓글 묶음을 현재 독후감의 댓글 목록 항목으로 표시한다
   *
   * @author HanWon.Jang
   * @param thread 화면에 표시할 부모 댓글과 답글 묶음
   * @return 부모 댓글과 답글 목록 화면
   */
  const renderReplyThread = (thread: ReplyThreadType): ReactNode => {
    // 현재 독후감 번호와 상호작용 상태를 적용한 댓글 묶음을 반환한다
    return (
      <ReplyThread
        key={`${report.reptNumb}-thread-${thread.parentReply.replNumb}`}
        reportNumb={report.reptNumb}
        thread={thread}
        controller={controller}
      />
    );
  };

  // 조회 상태에 맞는 댓글 목록과 등록 폼을 포함한 바텀시트 화면을 반환한다
  return (
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
        ref={controller.sheetRef}
        role="dialog"
        aria-modal="true"
        aria-label={
          report.userNick
            ? `${report.userNick}님의 독후감 댓글`
            : /* "댓글" */ message("frontend.report.comment.aria")
        }
        style={controller.sheetStyle}
      >
        {/* 댓글 바텀시트 드래그와 키보드 닫기 영역 */}
        {/* "아래로 당겨 닫기" */}
        <div
          className={styles.sheetHandle}
          aria-label="아래로 당겨 닫기"
          role="button"
          tabIndex={0}
          onKeyDown={controller.handleHandleKeyDown}
          onPointerDown={controller.handlePointerDown}
          onPointerMove={controller.handlePointerMove}
          onPointerUp={controller.handlePointerEnd}
          onPointerCancel={controller.handlePointerEnd}
        />

        {/* 댓글 목록과 빈 목록 안내 영역 */}
        <div className={styles.commentSheetBody}>
          {controller.replyListQuery.isPending ? (
            /* 댓글 목록 조회 진행 상태 영역 */
            /* "목록 조회중" */
            <Loading
              title={message("frontend.common.loadingList")}
              isFullScreen={false}
            />
          ) : controller.replyListQuery.isError ? (
            /* 댓글 목록 조회 실패 안내 영역 */
            <div className={styles.commentEmpty}>
              <p className={styles.commentEmptyText}>
                {getApiErrorMessage(
                  controller.replyListQuery.error,
                  message("frontend.common.tryAgain"),
                )}
              </p>
            </div>
          ) : controller.replyThreads.length > 0 ? (
            /* 등록된 댓글 목록 영역 */
            <div className={styles.replyList}>
              {controller.replyThreads.map(renderReplyThread)}
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

        {/* 댓글 입력과 등록 또는 수정 버튼 영역 */}
        <div className={styles.commentComposer}>
          {controller.editingReplyNumb !== null ? (
            /* 댓글 수정 상태와 취소 제어 영역 */
            <div className={styles.commentEditHeader}>
              {/* "댓글 수정 중" */}
              <span>{message("frontend.reply.editing")}</span>
              <button
                className={styles.commentEditCancelButton}
                type="button"
                onClick={controller.handleCancelEditReply}
              >
                {/* "취소" */}
                {message("frontend.common.cancel")}
              </button>
            </div>
          ) : null}
          <form
            className={styles.commentForm}
            onSubmit={controller.handleSubmit}
          >
            {/* "댓글을 입력해주세요." */}
            {/* "댓글 입력" */}
            <input
              ref={controller.commentInputRef}
              className={styles.commentInput}
              type="text"
              value={controller.commentInput}
              maxLength={500}
              placeholder={message("frontend.reply.inputPlaceholder")}
              aria-label={message("frontend.reply.inputAria")}
              onChange={controller.handleCommentInputChange}
            />
            <button
              className={styles.commentSubmitButton}
              type="submit"
              disabled={controller.isSubmitDisabled}
            >
              {controller.editingReplyNumb !== null ? (
                <>
                  {/* "수정 저장" */}
                  {message("frontend.reply.saveEdit")}
                </>
              ) : (
                <>
                  {/* "등록" */}
                  {message("frontend.reply.create")}
                </>
              )}
            </button>
          </form>
        </div>
      </section>
    </div>
  );
};

export default ReplySheetView;
