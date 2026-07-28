import { createPortal } from "react-dom";
import { getApiErrorMessage } from "@/app/api/resultData";
import type { PublicReportType } from "@/features/Book/types/book.type";
import { message } from "@/app/messages/message";
import { useReplySheet } from "@/features/reply/hooks/useReplySheet";
import { useReplyList } from "@/features/reply/hooks/useReplyList";
import { useSetReplyForm } from "@/features/reply/hooks/useSetReplyForm";
import * as styles from "./ReplySheet.css";

const DEFAULT_PROFILE_IMAGE = "/img/common/icon-user.svg";

type ReplySheetProps = {
  report: Pick<PublicReportType, "reptNumb" | "userNick">;
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
    handleSubmit,
  } = useSetReplyForm({
    reptNumb: report.reptNumb,
  });
  // 선택한 독후감의 댓글과 답글 목록을 서버 캐시에서 조회한다
  const replyListQuery = useReplyList(report.reptNumb);
  const replies = replyListQuery.data?.data ?? [];

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
        aria-label={`${report.userNick}님의 독후감 댓글`}
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
          ) : replies.length > 0 ? (
            /* 등록된 댓글 목록 영역 */
            <div className={styles.replyList}>
              {replies.map((reply) => (
                /* 등록된 댓글 개별 항목 영역 */
                <article
                  className={styles.replyItem}
                  key={`${report.reptNumb}-${reply.replNumb}`}
                >
                  {/* 댓글 작성자 프로필 영역 */}
                  <div className={styles.replyItemTop}>
                    <div className={styles.replyItemHeader}>
                      <div className={styles.replyWriterArea}>
                        <img
                          className={styles.replyProfileImage}
                          src={reply.porfPath || DEFAULT_PROFILE_IMAGE}
                          alt=""
                        />
                        <span className={styles.replyWriter}>
                          {reply.userNick || "-"}
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* 댓글 내용 영역 */}
                  <div className={styles.replyContentWrap}>
                    <p className={styles.replyContent}>
                      {reply.replCntn}
                    </p>
                  </div>

                  {/* 댓글 좋아요와 답글 버튼 영역 */}
                  <div className={styles.replyItemMetrics}>
                    {/* "좋아요" */}
                    <button
                      className={styles.replyMetricButton}
                      type="button"
                      aria-label="좋아요"
                      aria-pressed={reply.likeYsno === "Y"}
                    >
                      {reply.likeYsno === "Y" ? (
                        <img
                          src="/img/icons/icon-heart-fill.svg"
                          alt="좋아요"
                        />
                      ) : (
                        <img
                          src="/img/icons/icon-heart.svg"
                          alt="좋아요"
                        />
                      )}
                      <span>{Number(reply.likeCnt) || 0}</span>
                    </button>

                    {/* "답글 보기" */}
                    <button
                      className={styles.replyAnswerButton}
                      type="button"
                      aria-label="답글 보기"
                    >
                      <img
                        src="/img/icons/icon-comment.svg"
                        alt="답글"
                      />
                      <span>{Number(reply.replCnt) || 0}</span>
                    </button>
                  </div>
                </article>
              ))}
            </div>
          ) : (
            /* 댓글 빈 목록 안내 영역 */
            <div className={styles.commentEmpty}>
              <img
                className={styles.commentEmptyIcon}
                src="/img/icons/noti-COMMENT.svg"
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
